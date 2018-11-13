package com.bingor.camerahelperlib.util

import android.annotation.TargetApi
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.hardware.camera2.params.StreamConfigurationMap
import android.os.Build
import android.util.Log
import android.util.Size

/**
 * Created by HXB on 2018/11/13.
 */
class TestUtils {

    companion object {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        fun printCameraOutputFormat(map: StreamConfigurationMap) {
            for (opFormat: Int in map.outputFormats) {
                when (opFormat) {
                    ImageFormat.JPEG -> {
                        Log.d("HXB", "ImageFormat.JPEG----------------->")
                    }
                    ImageFormat.RAW10 -> {
                        Log.d("HXB", "ImageFormat.RAW10----------------->")
                    }
                    ImageFormat.RAW12 -> {
                        Log.d("HXB", "ImageFormat.RAW12----------------->")
                    }
                    ImageFormat.RAW_PRIVATE -> {
                        Log.d("HXB", "ImageFormat.RAW_PRIVATE----------------->")
                    }
                    ImageFormat.RAW_SENSOR -> {
                        Log.d("HXB", "ImageFormat.RAW_SENSOR----------------->")
                    }
                    ImageFormat.NV16 -> {
                        Log.d("HXB", "ImageFormat.NV16----------------->")
                    }
                    ImageFormat.NV21 -> {
                        Log.d("HXB", "ImageFormat.NV21----------------->")
                    }
                    ImageFormat.RGB_565 -> {
                        Log.d("HXB", "ImageFormat.RGB_565----------------->")
                    }
                    ImageFormat.FLEX_RGBA_8888 -> {
                        Log.d("HXB", "ImageFormat.FLEX_RGBA_8888----------------->")
                    }
                    ImageFormat.FLEX_RGB_888 -> {
                        Log.d("HXB", "ImageFormat.FLEX_RGB_888----------------->")
                    }
                    ImageFormat.YUV_420_888 -> {
                        Log.d("HXB", "ImageFormat.YUV_420_888----------------->")
                    }
                    ImageFormat.YUV_422_888 -> {
                        Log.d("HXB", "ImageFormat.YUV_422_888----------------->")
                    }
                    ImageFormat.YUV_444_888 -> {
                        Log.d("HXB", "ImageFormat.YUV_444_888----------------->")
                    }
                    ImageFormat.YUY2 -> {
                        Log.d("HXB", "ImageFormat.YUY2----------------->")
                    }
                    ImageFormat.UNKNOWN -> {
                        Log.d("HXB", "ImageFormat.YUY2----------------->")
                    }
                    ImageFormat.PRIVATE -> {
                        Log.d("HXB", "ImageFormat.YUY2----------------->")
                    }
                    ImageFormat.DEPTH16 -> {
                        Log.d("HXB", "ImageFormat.YUY2----------------->")
                    }
                    ImageFormat.DEPTH_POINT_CLOUD -> {
                        Log.d("HXB", "ImageFormat.YUY2----------------->")
                    }
                }
                var sizesA = map.getOutputSizes(opFormat)
                for (size: Size in sizesA) {
                    Log.d("HXB", size.toString())
                }
                Log.d("HXB", ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")

                when (opFormat) {
                    PixelFormat.OPAQUE -> {
                        Log.d("HXB", "PixelFormat.OPAQUE~~~~~~~~~~~~~~~~~~~~>")
                    }
                    PixelFormat.RGBA_1010102 -> {
                        Log.d("HXB", "PixelFormat.RGBA_1010102~~~~~~~~~~~~~~~~~~~~>")
                    }
                    PixelFormat.RGBA_8888 -> {
                        Log.d("HXB", "PixelFormat.RGBA_8888~~~~~~~~~~~~~~~~~~~~>")
                    }
                    PixelFormat.RGBA_F16 -> {
                        Log.d("HXB", "PixelFormat.RGBA_F16~~~~~~~~~~~~~~~~~~~~>")
                    }
                    PixelFormat.RGBX_8888 -> {
                        Log.d("HXB", "PixelFormat.RGBX_8888~~~~~~~~~~~~~~~~~~~~>")
                    }
                    PixelFormat.RGB_565 -> {
                        Log.d("HXB", "PixelFormat.RGB_565~~~~~~~~~~~~~~~~~~~~>")
                    }
                    PixelFormat.RGB_888 -> {
                        Log.d("HXB", "PixelFormat.RGB_888~~~~~~~~~~~~~~~~~~~~>")
                    }
                    PixelFormat.TRANSLUCENT -> {
                        Log.d("HXB", "PixelFormat.TRANSLUCENT~~~~~~~~~~~~~~~~~~~~>")
                    }
                    PixelFormat.TRANSPARENT -> {
                        Log.d("HXB", "PixelFormat.TRANSPARENT~~~~~~~~~~~~~~~~~~~~>")
                    }
                    PixelFormat.UNKNOWN -> {
                        Log.d("HXB", "PixelFormat.UNKNOWN~~~~~~~~~~~~~~~~~~~~>")
                    }
                    PixelFormat.A_8 -> {
                        Log.d("HXB", "PixelFormat.A_8~~~~~~~~~~~~~~~~~~~~>")
                    }
                    PixelFormat.LA_88 -> {
                        Log.d("HXB", "PixelFormat.LA_88~~~~~~~~~~~~~~~~~~~~>")
                    }
                    PixelFormat.L_8 -> {
                        Log.d("HXB", "PixelFormat.L_8~~~~~~~~~~~~~~~~~~~~>")
                    }
                    PixelFormat.RGBA_4444 -> {
                        Log.d("HXB", "PixelFormat.RGBA_4444~~~~~~~~~~~~~~~~~~~~>")
                    }
                    PixelFormat.RGBA_5551 -> {
                        Log.d("HXB", "PixelFormat.RGBA_5551~~~~~~~~~~~~~~~~~~~~>")
                    }
                    PixelFormat.RGB_332 -> {
                        Log.d("HXB", "PixelFormat.RGB_332~~~~~~~~~~~~~~~~~~~~>")
                    }
                    PixelFormat.YCbCr_420_SP -> {
                        Log.d("HXB", "PixelFormat.YCbCr_420_SP~~~~~~~~~~~~~~~~~~~~>")
                    }
                    PixelFormat.YCbCr_422_I -> {
                        Log.d("HXB", "PixelFormat.YCbCr_422_I~~~~~~~~~~~~~~~~~~~~>")
                    }
                    PixelFormat.YCbCr_422_SP -> {
                        Log.d("HXB", "PixelFormat.YCbCr_422_SP~~~~~~~~~~~~~~~~~~~~>")
                    }
                    PixelFormat.YCbCr_422_I -> {
                        Log.d("HXB", "PixelFormat.YCbCr_422_I~~~~~~~~~~~~~~~~~~~~>")
                    }
                }
                var sizesB = map.getOutputSizes(opFormat)
                for (size: Size in sizesB) {
                    Log.d("HXB", size.toString())
                }
                Log.d("HXB", "==============================================")

//                        if (opFormat == imageFormat || opFormat == pixelFormat) {
//                            //找到符合要求的摄像头
//                            this.cameraId = cameraId
////                            map.getOutputSizes()
//                            break
//                        }
            }
        }


    }
}