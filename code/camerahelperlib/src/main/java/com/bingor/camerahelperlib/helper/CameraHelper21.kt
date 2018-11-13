package com.bingor.camerahelperlib.helper

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.hardware.camera2.*
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.os.Message
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.SurfaceHolder
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import com.bingor.camerahelperlib.util.Utils
import java.util.*

/**
 * Created by HXB on 2018/10/26.
 */

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class CameraHelper21 : CameraHelper {
    //摄像头管理
    var manager: CameraManager
    //摄像头实例
    var cameraDevice: CameraDevice? = null
    //用于绘制的Holder
    var surfaceHolder: SurfaceHolder? = null
    //镜头方向（前置后置）
    var facing: Int = 0
    //输出格式
    var format: Int = 0

    var imageReader: ImageReader? = null

    constructor(context: Context, surfaceHolder: SurfaceHolder?) : super(context) {
        manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        this.surfaceHolder = surfaceHolder;
    }

    @SuppressLint("MissingPermission")
    override fun openCamera(facing: Int, format: Int) {
        this.facing = facing;
        this.format = format;
        findCamera(facing, format)
        if (cameraId.equals("-1")) {
            throw Exception("no match camera")
        } else {
            try {
                //打开相机预览
                manager.openCamera(cameraId, CameraStateCallBack(),
                        object : Handler() {
                            override fun handleMessage(msg: Message?) {
                                super.handleMessage(msg)
                                msg?.let {
                                    Log.d("HXB", "handleMessage==" + msg.what)
                                }
                            }
                        })
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                throw  RuntimeException("Interrupted while trying to lock camera opening.", e)
            }
        }
    }

    /**
     * 寻找可用镜头
     * imageFormat和pixelFormat并不需要同时设置，imageFormat往往都有对应的pixelFormat
     * @param format 输出格式
     * 所有的值都在这个里面
     * ImageFormat
     * PixelFormat
     */
    private fun findCamera(facing: Int, format: Int) {
        for (cameraId: String in manager.getCameraIdList()) {
            //获取相机的相关参数
            var characteristics = manager.getCameraCharacteristics(cameraId)
            // 摄像头类型
            var cameraFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (cameraFacing == facing) {
                var map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) as StreamConfigurationMap
                if (map != null) {
                    /**
                     * 支持的输出格式
                     * map.outputFormats
                     */
                    for (opFormat: Int in map.outputFormats) {
                        if (opFormat == format) {
                            //找到符合要求的摄像头
                            this.cameraId = cameraId
                            findSuitablePreviewSize(map, opFormat)
                            imageReader = ImageReader.newInstance(actualSize!!.getWidth(), actualSize!!.getHeight(), opFormat, /*maxImages*/2);
                            break
                        }
                    }
                }
                break
            }
        }
    }

    /**
     * 找到最接近预设比例的预览尺寸
     */
    private fun findSuitablePreviewSize(map: StreamConfigurationMap, format: Int) {
        var sizes = map.getOutputSizes(format)
        if (previewSize != null) {
            var ratioDelta = 100000f

            var ratioRequest: Float = previewSize!!.width / (previewSize!!.height * 1f)
            for (size: Size in sizes) {
                var temp = Math.abs((size.width / (size.height * 1f)) - ratioRequest)
                if (temp < ratioDelta) {
                    actualSize = size
                    ratioDelta = temp
                }
            }
        } else {
            actualSize = Collections.max(
                    Arrays.asList(*map.getOutputSizes(format)),
                    CompareSizesByArea())
        }
    }

    /**
     * 为相机预览创建新的CameraCaptureSession
     */
    private fun createCameraPreviewSession() {
        cameraDevice?.createCaptureSession(
                Arrays.asList(surfaceHolder?.getSurface()),
                CameraCaptureSessionStateCallback(),
                object : Handler() {
                    override fun handleMessage(msg: Message?) {
                        super.handleMessage(msg)
                    }
                })
    }

    /**
     * 相机画面捕捉任务状态回调
     */
    inner class CameraCaptureSessionStateCallback : CameraCaptureSession.StateCallback {
        //相机画面捕捉任务
        var cameraCaptureSession: CameraCaptureSession? = null
        var previewRequestBuilder: CaptureRequest.Builder? = null

        constructor() {
            previewRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequestBuilder?.addTarget(surfaceHolder?.getSurface())
        }

        override fun onConfigureFailed(session: CameraCaptureSession) {
            Log.e("HXB", " onConfigureFailed 开启预览失败");
        }

        override fun onConfigured(session: CameraCaptureSession) {
            // 相机已经关闭
            if (null == cameraDevice) {
                return
            }
            // 会话准备好后，我们开始显示预览
            this.cameraCaptureSession = cameraCaptureSession;
            try {
                //自动曝光
                previewRequestBuilder?.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
                //自动对焦
                previewRequestBuilder?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
                //开启相机预览并添加事件
                //发送请求
                cameraCaptureSession?.setRepeatingRequest(
                        previewRequestBuilder?.build(),
                        null,
                        object : Handler() {
                            override fun handleMessage(msg: Message?) {
                                super.handleMessage(msg)
                            }
                        });
                Log.e("HXB", " 开启相机预览并添加事件");
            } catch (e: CameraAccessException) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 相机状态回调
     */
    inner class CameraStateCallBack : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCameraPreviewSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
            cameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            cameraDevice = null
        }
    }

    internal class CompareSizesByArea : Comparator<Size> {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun compare(lhs: Size, rhs: Size): Int {
            // 我们在这里投放，以确保乘法不会溢出
            return java.lang.Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
        }

    }
}