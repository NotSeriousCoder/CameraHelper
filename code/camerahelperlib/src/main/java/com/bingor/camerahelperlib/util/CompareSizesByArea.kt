package com.bingor.camerahelperlib.util

import android.os.Build
import android.util.Size
import androidx.annotation.RequiresApi

/**
 * Created by Bingor on 2019/2/1.
 */
class CompareSizesByArea : Comparator<Size> {
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun compare(lhs: Size, rhs: Size): Int {
        return java.lang.Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
    }
}