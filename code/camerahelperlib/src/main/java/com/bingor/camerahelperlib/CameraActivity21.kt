package com.bingor.camerahelperlib

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.StreamConfigurationMap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Size

/**
 * Created by HXB on 2018/10/25.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class CameraActivity21 : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        var manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        //获取可用摄像头列表
        for (cameraId: String in manager.getCameraIdList()) {
            Log.d("HXB", "cameraId==" + cameraId);
            //获取相机的相关参数
            var characteristics = manager.getCameraCharacteristics(cameraId)
            // 摄像头类型
            var facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            when (facing) {
                CameraCharacteristics.LENS_FACING_FRONT -> {
                    //前置摄像头
                }
                CameraCharacteristics.LENS_FACING_BACK -> {
                    //后置主摄像头
                }
                CameraCharacteristics.LENS_FACING_EXTERNAL -> {
                    //后置其他摄像头
                }
            }

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
                    Log.d("HXB", "opFormat==" + opFormat)
                    // 检查闪光灯是否支持。
                    var available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) as Boolean
                    Log.d("HXB", "支持闪光灯==" + available)
                    /**
                     * 不同的输出格式会对应不同支持的输出分辨率
                     */
                    for (size: Size in map.getOutputSizes(opFormat)) {
                        Log.d("HXB", "size==" + size.toString());
                    }
                    Log.d("HXB", "===============================")
                }

                Log.d("HXB", "===============================")

            }

        }


//        try {
//            for (String cameraId : manager.getCameraIdList()) {


//                mFlashSupported = available == null ? false : available;
//                mCameraId = cameraId;
//                Log.e(TAG, " 相机可用 ");
//                return;
//            }
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        } catch (NullPointerException e) {
//            //不支持Camera2API
//        }
    }
}