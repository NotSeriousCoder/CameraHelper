package com.bingor.camerahelperlib.callback

import android.graphics.SurfaceTexture
import com.bingor.camerahelperlib.other.Size


/**
 * Created by Bingor on 2019/2/13.
 */
interface OnCameraHelperListener {
    fun onPreviewSizeChanged(previewSize: Size, vertical: Boolean)

    fun getSurfaceTexture(): SurfaceTexture
}