package com.bingor.camerahelperlib.helper

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.hardware.camera2.params.StreamConfigurationMap
import android.os.Build
import android.os.Handler
import android.os.Message
import android.util.Log
import android.util.Size
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import java.util.*

/**
 * Created by HXB on 2018/10/26.
 */

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class CameraHelper21 : CameraHelper {

    constructor(context: Context) : super(context) {
        manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    @SuppressLint("MissingPermission")
    override fun openCamera(facing: Int, imageFormat: Int, pixelFormat: Int) {
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
                     * 所有的值都在这个里面
                     * ImageFormat
                     * PixelFormat
                     */
                    for (opFormat: Int in map.outputFormats) {
                        if (opFormat == imageFormat || opFormat == pixelFormat) {
                            //找到符合要求的摄像头
                            this.cameraId = cameraId
                            break
                        }
                    }
                }
                break
            }
        }

        if (cameraId.equals("-1")) {
            throw Exception("no match camera")
        } else {
            try {
                //打开相机预览
                manager.openCamera(cameraId,
                        object : CameraDevice.StateCallback() {
                            override fun onOpened(camera: CameraDevice?) {
                                Log.d("HXB", "onOpened===========")
                            }

                            override fun onDisconnected(camera: CameraDevice?) {
                                Log.d("HXB", "onDisconnected===========")
                            }

                            override fun onError(camera: CameraDevice?, error: Int) {
                                Log.d("HXB", "onError===========")
                            }
                        },
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
     * 为相机预览创建新的CameraCaptureSession
     */
    private fun createCameraPreviewSession(camera: CameraDevice) {
        //设置了一个具有输出Surface的CaptureRequest.Builder。
        var previewRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

        try {
            mPreviewRequestBuilder.addTarget(mSurfaceHolder.getSurface());
            //创建一个CameraCaptureSession来进行相机预览。
            mCameraDevice.createCaptureSession(Arrays.asList(mSurfaceHolder.getSurface()),
                    new CameraCaptureSession . StateCallback () {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // 相机已经关闭
                            if (null == mCameraDevice) {
                                return;
                            }
                            // 会话准备好后，我们开始显示预览
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // 自动对焦应
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // 闪光灯
                                setAutoFlash(mPreviewRequestBuilder);
                                // 开启相机预览并添加事件
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                //发送请求
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        null, mBackgroundHandler);
                                Log.e(TAG, " 开启相机预览并添加事件");
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            Log.e(TAG, " onConfigureFailed 开启预览失败");
                        }
                    }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, " CameraAccessException 开启预览失败");
            e.printStackTrace();
        }
    }


}