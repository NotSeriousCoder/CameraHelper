package com.bingor.camerahelperlib.helper

import android.content.Context
import android.hardware.camera2.CameraManager

/**
 * Created by HXB on 2018/10/26.
 */
abstract class CameraHelper {
    protected var context: Context
    protected lateinit var manager: CameraManager
    protected var cameraId = "-1";

    constructor(context: Context) {
        this.context = context
    }

    open abstract fun openCamera(facing: Int, imageFormat: Int, pixelFormat: Int)
}