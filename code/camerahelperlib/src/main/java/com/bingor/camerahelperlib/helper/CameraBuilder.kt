package com.bingor.camerahelperlib.helper

import android.content.Context
import android.graphics.ImageFormat
import android.os.Build
import com.bingor.camerahelperlib.callback.OnCameraHelperListener

/**
 * Created by Bingor on 2018/12/24.
 */
class CameraBuilder {
    var context: Context
    @JvmField
    var cameraDirection = CameraDirection.BACK
    @JvmField
    var imageFormat: Int = ImageFormat.JPEG
    @JvmField
    var onCameraHelperListener: OnCameraHelperListener? = null
    @JvmField
    var imageRatio: Float = CameraHelper.IMAGE_RATIO_16_9

    constructor(context: Context) {
        this.context = context

    }


    fun setCameraDirection(cameraDirection: CameraDirection): CameraBuilder {
        this.cameraDirection = cameraDirection
        return this
    }

    fun setImageFormat(imageFormat: Int): CameraBuilder {
        this.imageFormat = imageFormat
        return this
    }

    fun setOnCameraHelperListener(onCameraHelperListener: OnCameraHelperListener): CameraBuilder {
        this.onCameraHelperListener = onCameraHelperListener
        return this
    }

    fun setImageRatio(imageRatio: Float): CameraBuilder {
        this.imageRatio = imageRatio
        return this
    }


    fun create(): CameraHelper? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            var helper = CameraHelper21(context, cameraDirection)
            helper.imageFormat = imageFormat
            helper.onCameraHelperListener = onCameraHelperListener
            helper.imageRatio = imageRatio
            return helper
        } else {
            return null
        }
    }

}

enum class CameraDirection {
    FRONT, BACK
}