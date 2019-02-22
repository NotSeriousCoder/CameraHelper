package com.bingor.camerahelperlib.util

import com.bingor.camerahelperlib.other.Size
import com.bingor.utillib.log.Log
import java.util.ArrayList

/**
 * Created by Bingor on 2019/2/20.
 */
class KTUtils {
    companion object {
        fun checkAllNotNull(vararg args: Any?): Boolean {
            return args.all { it != null }
        }

        fun transformSize(sizes: Array<android.util.Size>): Array<Size> {
            var newArr: Array<Size?> = arrayOfNulls(sizes.size)
            for (i in 0..sizes.size - 1) {
                newArr[i] = Size.parseSize(sizes[i].toString())
            }
            return newArr as Array<Size>
        }


        fun transformSize(sizes: MutableList<android.util.Size>): MutableList<Size> {
            var newArr = ArrayList<Size>()
            for (size in sizes) {
//                Log.d("size==" + size.toString())
                newArr.add(Size.parseSize(size.toString()))
            }
            return newArr
        }
    }
}