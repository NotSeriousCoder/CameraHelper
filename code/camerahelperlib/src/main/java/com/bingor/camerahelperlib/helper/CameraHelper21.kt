package com.bingor.camerahelperlib.helper

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.view.Surface
import android.view.SurfaceHolder
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import com.bingor.camerahelperlib.callback.OnCameraHelperListener
import com.bingor.camerahelperlib.other.Size
import com.bingor.camerahelperlib.util.KTUtils
import com.bingor.utillib.hardware.ScreenUtil
import com.bingor.utillib.log.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by HXB on 2018/10/26.
 */

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class CameraHelper21 : CameraHelper {
    val MAX_IMAGES_4_READER = 2
    //摄像头管理
    var manager: CameraManager
    //摄像头实例
    var cameraDevice: CameraDevice? = null
    //用于绘制的Holder
    var surfaceHolder: SurfaceHolder? = null

    //传感器方向
    var sensorOrientation: Int = 0

    var imageReader: ImageReader? = null
    var cameraStateCallBack: CameraStateCallBack
    var imageAvailableCallBack: ImageAvailableCallBack
    var captureCallback: CHCaptureCallback
    var onCameraHelperListener: OnCameraHelperListener? = null

    var backgroundHandler: Handler? = null
    var backgroundThread: HandlerThread? = null

    var previewRequestBuilder: CaptureRequest.Builder? = null
    var cameraCaptureSession: CameraCaptureSession? = null
    var previewRequest: CaptureRequest? = null

    var capture = false

    constructor(context: Context, cameraDirection: CameraDirection) : super(context, cameraDirection) {
        manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraStateCallBack = CameraStateCallBack()
        imageAvailableCallBack = ImageAvailableCallBack()
        captureCallback = CHCaptureCallback()
    }


    override fun init(surfaceWidth: Int, surfaceHeight: Int) {
        if (surfaceWidth == 0 || surfaceHeight == 0) {
            return
        }
        this.surfaceWidth = surfaceWidth
        this.surfaceHeight = surfaceHeight

        findCamera()
        setUpCameraOutputs()
    }

    @SuppressLint("MissingPermission")
    override fun openCamera() {
        if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
            throw RuntimeException("Time out waiting to lock camera opening.")
        }
        manager.openCamera(cameraId, cameraStateCallBack, backgroundHandler)
    }

    /**
     * 寻找可用镜头
     * imageFormat和pixelFormat并不需要同时设置，imageFormat往往都有对应的pixelFormat
     * @param format 输出格式
     * 所有的值都在这个里面
     * ImageFormat
     * PixelFormat
     */
    private fun findCamera() {
        for (cameraId: String in manager.getCameraIdList()) {
            val characteristics = manager.getCameraCharacteristics(cameraId)

            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (facing == null ||
                    (cameraDirection == CameraDirection.FRONT && facing != CameraCharacteristics.LENS_FACING_FRONT) ||
                    (cameraDirection == CameraDirection.BACK && facing != CameraCharacteristics.LENS_FACING_BACK)) {
                //如果摄像头方向与用户指定的不一致，跳过
                continue
            } else {
                this.cameraId = cameraId
                break
            }
        }
    }

    /**
     * 为相机预览创建新的CameraCaptureSession
     */
    private fun createCameraPreviewSession() {
        onCameraHelperListener?.let {
            val texture = it.getSurfaceTexture()!!
            // We configure the size of default buffer to be the size of camera preview we want.
            texture!!.setDefaultBufferSize(previewSize!!.getWidth(), previewSize!!.getHeight())

            // This is the output Surface we need to start preview.
            val surface = Surface(texture)

            // We set up a CaptureRequest.Builder with the output Surface.
            previewRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequestBuilder?.addTarget(surface)

            // Here, we create a CameraCaptureSession for camera preview.
            cameraDevice?.createCaptureSession(Arrays.asList(surface, imageReader!!.getSurface()), CameraCaptureSessionStateCallback(), null)
        }
    }

    override fun makeCameraPrepare() {
        backgroundThread = HandlerThread("CameraBackground")
        backgroundThread?.start()
        backgroundHandler = Handler(backgroundThread?.getLooper())
    }

    override fun makeCameraPause() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    private fun setUpCameraOutputs() {
        val characteristics = manager.getCameraCharacteristics(cameraId)

        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        if (map == null) {
            return
        }
        // For still image captures, we use the largest available size.
        var sizes = KTUtils.transformSize(Arrays.asList(*map.getOutputSizes(imageFormat)))
        selectSizeByRatio(sizes)
        val largest = Collections.max(sizes, CompareSizesByArea())
        imageReader = ImageReader.newInstance(largest.width, largest.height, imageFormat, MAX_IMAGES_4_READER)
        imageReader?.setOnImageAvailableListener(imageAvailableCallBack, backgroundHandler)


        // Find out if we need to swap dimension to get the preview size relative to sensor
        // coordinate.
        val displayRotation = ScreenUtil.getScreenOrientation(context)

        sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
        var swappedDimensions = false
        when (displayRotation) {
            Configuration.ORIENTATION_PORTRAIT -> if (sensorOrientation == 90 || sensorOrientation == 270) {
                swappedDimensions = true
            }
            Configuration.ORIENTATION_LANDSCAPE -> if (sensorOrientation == 0 || sensorOrientation == 180) {
                swappedDimensions = true
            }
            else -> Log.e("HXB", "Display rotation is invalid: $displayRotation")
        }

        val displaySize = ScreenUtil.getScreenSize(context)

        var rotatedPreviewWidth = if (swappedDimensions) surfaceHeight else surfaceWidth
        var rotatedPreviewHeight = if (swappedDimensions) surfaceWidth else surfaceHeight
        var maxPreviewWidth = if (swappedDimensions) displaySize.y else displaySize.x
        var maxPreviewHeight = if (swappedDimensions) displaySize.x else displaySize.y


        previewSize = chooseOptimalSize(KTUtils.transformSize(map.getOutputSizes<SurfaceTexture>(SurfaceTexture::class.java)),
                rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                maxPreviewHeight, largest)

        onCameraHelperListener?.onPreviewSizeChanged(previewSize!!, displayRotation == Configuration.ORIENTATION_PORTRAIT)

        val available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
        flashSupported = available ?: false

        return
    }

    /**
     * @param surfaceSupportOptions 相机支持显示在视图上的配置集合
     * @param textureViewWidth 显示画面的View的宽度（已根据方向互调）
     * @param textureViewHeight 显示画面的View的高度（已根据方向互调）
     * @param screenWidth 屏幕宽度（已根据方向互调）
     * @param screenHeight 屏幕高度（已根据方向互调）
     * @param sensorOption 相机传过来的图像的配置
     */
    private fun chooseOptimalSize(surfaceSupportOptions: Array<Size>,
                                  textureViewWidth: Int, textureViewHeight: Int,
                                  screenWidth: Int, screenHeight: Int,
                                  sensorOption: Size): Size {
        // Collect the supported resolutions that are at least as big as the preview Surface
        val bigEnough = ArrayList<Size>()
        // Collect the supported resolutions that are smaller than the preview Surface
        val notBigEnough = ArrayList<Size>()
        val w = sensorOption.width
        val h = sensorOption.height
        for (option in surfaceSupportOptions) {
            if (option.width <= screenWidth && option.height <= screenHeight &&
                    option.height == option.width * h / w) {
                if (option.width >= textureViewWidth && option.height >= textureViewHeight) {
                    bigEnough.add(option)
                } else {
                    notBigEnough.add(option)
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size > 0) {
            return Collections.min(bigEnough, CompareSizesByArea())
        } else if (notBigEnough.size > 0) {
            return Collections.max(notBigEnough, CompareSizesByArea())
        } else {
            return surfaceSupportOptions[0]
        }
    }


    fun selectSizeByRatio(sizes: MutableList<Size>) {
        var i = 0
        while (i < sizes.size) {
            val size = sizes[i]
            if (size.width.toFloat() / size.height != imageRatio) {
                sizes.removeAt(i)
                i--
            }
            i++
        }
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * [.mCaptureCallback] from both [.lockFocus].
     */
    private fun captureStillPicture() {
        val captureBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        if (captureBuilder != null) {
            captureBuilder.addTarget(imageReader?.getSurface())
            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            setAutoFlash(captureBuilder)

            val rotation = ScreenUtil.getScreenRotation(context)
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation))

            val CaptureCallback = object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(@NonNull session: CameraCaptureSession,
                                                @NonNull request: CaptureRequest,
                                                @NonNull result: TotalCaptureResult) {
//                    showToast("Saved: $mFile")
                    Log.d(file.toString())
                    unlockFocus()
                }
            }

            cameraCaptureSession?.let {
                it.stopRepeating()
                it?.abortCaptures()
                it?.capture(captureBuilder.build(), CaptureCallback, null)
            }
        }
    }

    private fun getOrientation(rotation: Int): Int {
        when (rotation) {
            0 -> return (90 + sensorOrientation + 270) % 360
            90 -> return (0 + sensorOrientation + 270) % 360
            180 -> return (270 + sensorOrientation + 270) % 360
            270 -> return (180 + sensorOrientation + 270) % 360
        }
        return 0;
    }

    private fun setAutoFlash(requestBuilder: CaptureRequest.Builder) {
        if (flashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
        }
    }

    override fun setFlash(open: Boolean) {
//        var builder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        previewRequestBuilder?.set(CaptureRequest.FLASH_MODE, if (open) CaptureRequest.FLASH_MODE_TORCH else CaptureRequest.FLASH_MODE_OFF)
        cameraCaptureSession?.setRepeatingRequest(previewRequestBuilder?.build(), object : CameraCaptureSession.CaptureCallback() {}, backgroundHandler)
    }

    override fun closeCamera() {
        makeCameraPause()
        try {
            cameraOpenCloseLock.acquire()
            cameraCaptureSession?.let {
                it.close()
                cameraCaptureSession = null
            }
            cameraDevice?.let {
                it.close()
                cameraDevice = null
            }
            imageReader?.let {
                it.close()
                imageReader = null
            }
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    override fun isFlash(): Boolean {
        if (previewRequestBuilder == null) {
            return false
        } else {
            return previewRequestBuilder!!.get(CaptureRequest.FLASH_MODE) == CaptureRequest.FLASH_MODE_TORCH
        }
    }

    private fun unlockFocus() {
        if (KTUtils.checkAllNotNull(previewRequestBuilder, cameraCaptureSession)) {
            // Reset the auto-focus trigger
            previewRequestBuilder?.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
            setAutoFlash(previewRequestBuilder!!)
            cameraCaptureSession?.capture(previewRequestBuilder?.build(), captureCallback, backgroundHandler)
            // After this, the camera will go back to the normal state of preview.
            actionState = STATE_PREVIEW
            cameraCaptureSession?.setRepeatingRequest(previewRequest, captureCallback,
                    backgroundHandler)
        }
    }

    private fun runPrecaptureSequence() {
        if (KTUtils.checkAllNotNull(previewRequestBuilder, cameraCaptureSession)) {
            // This is how to tell the camera to trigger.
            previewRequestBuilder?.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START)
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.--------------
            actionState = STATE_WAITING_PRECAPTURE
        }
        cameraCaptureSession?.capture(previewRequestBuilder?.build(), captureCallback, backgroundHandler)
    }

    override fun capture() {
        file = File(context.getExternalFilesDir(null), SimpleDateFormat("yy-MM-dd HH:mm").format(Date(System.currentTimeMillis())) + ".jpg")
        actionState = STATE_WAITING_LOCK
        capture = true
    }

    /**
     * 相机画面捕捉任务状态回调
     */
    inner class CameraCaptureSessionStateCallback : CameraCaptureSession.StateCallback() {
        override fun onConfigured(@NonNull cameraCaptureSession: CameraCaptureSession) {
            cameraDevice?.let {
                this@CameraHelper21.cameraCaptureSession = cameraCaptureSession
//                previewRequestBuilder?.addTarget(imageReader?.surface)
                //3A设为自动控制模式
                previewRequestBuilder?.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)
                //色差校正模式
                previewRequestBuilder?.set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_HIGH_QUALITY)
                previewRequestBuilder?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)

                previewRequest = previewRequestBuilder?.build()
                cameraCaptureSession.setRepeatingRequest(previewRequest, captureCallback, backgroundHandler)
            }
        }

        override fun onConfigureFailed(@NonNull cameraCaptureSession: CameraCaptureSession) {
            Log.d("HXB", "onConfigureFailed=========")
        }
    }

    /**
     * 相机状态回调
     */
    inner class CameraStateCallBack : CameraDevice.StateCallback() {
        override fun onOpened(@NonNull cameraDevice: CameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            cameraOpenCloseLock.release()
            this@CameraHelper21.cameraDevice = cameraDevice
            createCameraPreviewSession()
        }

        override fun onDisconnected(@NonNull cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            cameraDevice.close()
            this@CameraHelper21.cameraDevice = null
        }

        override fun onError(@NonNull cameraDevice: CameraDevice, error: Int) {
            cameraOpenCloseLock.release()
            cameraDevice.close()
            this@CameraHelper21.cameraDevice = null
        }
    }

    inner class ImageAvailableCallBack : ImageReader.OnImageAvailableListener {
        override fun onImageAvailable(reader: ImageReader?) {
//            if (capture) {
//                capture = false
            reader?.let {
                backgroundHandler?.post(ImageSaver(reader.acquireNextImage(), file!!))
            }
//            }
        }

    }

    inner class CompareSizesByArea : Comparator<Size> {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun compare(lhs: Size, rhs: Size): Int {
            // 我们在这里投放，以确保乘法不会溢出
            return java.lang.Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
        }
    }

    inner class CHCaptureCallback : CameraCaptureSession.CaptureCallback() {


        //缓冲区发送失败
        override fun onCaptureBufferLost(session: CameraCaptureSession, request: CaptureRequest, target: Surface, frameNumber: Long) {
            super.onCaptureBufferLost(session, request, target, frameNumber)
        }

        //开始捕获图像
        override fun onCaptureStarted(session: CameraCaptureSession, request: CaptureRequest, timestamp: Long, frameNumber: Long) {
            super.onCaptureStarted(session, request, timestamp, frameNumber)
        }

        //图像捕获中
        override fun onCaptureProgressed(session: CameraCaptureSession, request: CaptureRequest, partialResult: CaptureResult) {
            super.onCaptureProgressed(session, request, partialResult)
            process(partialResult)
        }

        //图像捕获完全完毕并且元数据可用
        override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
            super.onCaptureCompleted(session, request, result)
            process(result)
        }

        //图像捕获失败
        override fun onCaptureFailed(session: CameraCaptureSession, request: CaptureRequest, failure: CaptureFailure) {
            super.onCaptureFailed(session, request, failure)
        }

        override fun onCaptureSequenceCompleted(session: CameraCaptureSession, sequenceId: Int, frameNumber: Long) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber)
        }

        override fun onCaptureSequenceAborted(session: CameraCaptureSession, sequenceId: Int) {
            super.onCaptureSequenceAborted(session, sequenceId)
        }

        private fun process(result: CaptureResult) {
            when (actionState) {
                STATE_PREVIEW -> {
                }
                STATE_WAITING_LOCK -> {
                    val afState = result.get(CaptureResult.CONTROL_AF_STATE)
                    if (afState == null) {
                        captureStillPicture()
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                        if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            actionState = STATE_PICTURE_TAKEN
                            captureStillPicture()
                        } else {
                            runPrecaptureSequence()
                        }
                    }
                }
                STATE_WAITING_PRECAPTURE -> {
                    Log.d("HXB", "mState == STATE_WAITING_PRECAPTURE")
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        actionState = STATE_WAITING_NON_PRECAPTURE
                    }
                }
                STATE_WAITING_NON_PRECAPTURE -> {
                    Log.d("HXB", "mState == STATE_WAITING_NON_PRECAPTURE")
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        actionState = STATE_PICTURE_TAKEN
                        captureStillPicture()
                    }
                }
            }
        }
    }

    inner class ImageSaver(val mImage: Image, val mFile: File) : Runnable {
        override fun run() {
            val buffer = mImage.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            var output: FileOutputStream? = null
            try {
                output = FileOutputStream(mFile)
                output.write(bytes)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                mImage.close()
                if (null != output) {
                    try {
                        output.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        }

    }
}

