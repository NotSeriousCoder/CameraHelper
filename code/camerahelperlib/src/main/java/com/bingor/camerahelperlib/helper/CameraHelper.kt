package com.bingor.camerahelperlib.helper

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import com.bingor.camerahelperlib.other.Size
import java.io.File
import java.util.concurrent.Semaphore

/**
 * Created by HXB on 2018/10/26.
 */
abstract class CameraHelper {
    //预览模式
    val STATE_PREVIEW = 0
    //等待锁定焦点
    val STATE_WAITING_LOCK = 1
    //等待曝光变成预捕获状态
    val STATE_WAITING_PRECAPTURE = 2
    //等待曝光变成预非捕获状态
    val STATE_WAITING_NON_PRECAPTURE = 3
    //图片以获取
    val STATE_PICTURE_TAKEN = 4

    companion object {
        val IMAGE_RATIO_16_9 = 16.toFloat() / 9
        val IMAGE_RATIO_4_3 = 4.toFloat() / 3
        val IMAGE_RATIO_1_1 = 1.toFloat() / 1
    }


    var context: Context
    //镜头方向（前置后置）
    var cameraDirection: CameraDirection
    var cameraId = "-1";
    var flashSupported = false;
    //相机资源锁，防止重复打开/关闭/未打开完成即关闭
    var cameraOpenCloseLock: Semaphore
    var previewSize: Size? = null
    //动作状态
    var actionState = STATE_PREVIEW

    /**
     * 图像输出格式
     * @see ImageFormat
     * @see PixelFormat
     */
    var imageFormat = 0
    //图片宽高比
    var imageRatio = IMAGE_RATIO_16_9
    var surfaceWidth = 0
    var surfaceHeight = 0

    var file: File? = null


    constructor(context: Context, cameraDirection: CameraDirection) {
        this.context = context
        this.cameraDirection = cameraDirection
        cameraOpenCloseLock = Semaphore(1)
    }

    open abstract fun openCamera()
    open abstract fun closeCamera()

    /**
     * 进行初始化工作
     */
    open abstract fun init(surfaceWidth: Int, surfaceHeight: Int)

    /**
     * 让摄像机进入待命状态，进行开启前的准备工作
     */
    open abstract fun makeCameraPrepare()

    /**
     * 让摄像机进入暂停状态，进行关闭前的准备工作
     */
    open abstract fun makeCameraPause()

    /**
     * 拍照
     */
    open abstract fun capture()

    /**
     * 开关闪光灯
     */
    open abstract fun setFlash(open: Boolean)

    /**
     * 闪光灯是否开启
     */
    open abstract fun isFlash(): Boolean
}