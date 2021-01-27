package com.tech.mvpframework.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.FaceDetector
import android.os.Handler
import android.os.Looper
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.View
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.concurrent.thread
import kotlin.math.min
import kotlin.math.roundToInt


/**
 *  create by Myking
 *  date : 2020/7/7 16:30
 *  description :
 */
object BitmapUtil {

    @Deprecated("识别率低，建议不要用")
    fun detectionFaceV1(b: Bitmap): Boolean {
        // 检测前必须转化为RGB_565格式
        val bitmap: Bitmap = b.copy(Bitmap.Config.RGB_565, true)
        // 设定最大可查的人脸数量
        val maxFaces = 5
        val faceDet = FaceDetector(bitmap.width, bitmap.height, maxFaces)
        // 将人脸数据存储到faceArray 中
        val faceArray: Array<FaceDetector.Face?> = arrayOfNulls<FaceDetector.Face>(maxFaces)
        // 返回找到图片中人脸的数量，同时把返回的脸部位置信息放到faceArray中，过程耗时
        val findFaceCount: Int = faceDet.findFaces(bitmap, faceArray)
        // 用完回收
        bitmap.recycle()
        return findFaceCount > 0
    }

    /**
     * 图片缩放比例
     */
    private const val BITMAP_SCALE = 0.4f

    /**
     * 最大模糊度(在0.0到25.0之间)
     */
    private const val BLUR_RADIUS = 15f

    /**
     * 从view中获取bitmap
     * @param view 需要生成bitmap的view
     * @param config
     * @return 根据view生成的bitmap
     */
    fun getBitmapFromView(view: View, config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
        val bitmap =
            Bitmap.createBitmap(view.width, view.height, config)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    /**
     * 模糊图片的具体方法
     *
     * @param context   上下文对象
     * @param image     需要模糊的图片
     * @return          模糊处理后的图片
     */
    fun blur(context: Context, image: Bitmap, blurRadius: Float = BLUR_RADIUS): Bitmap {
        // 计算图片缩小后的长宽
        val width = (image.width * BITMAP_SCALE).roundToInt()
        val height = (image.height * BITMAP_SCALE).roundToInt()

        // 将缩小后的图片做为预渲染的图片。
        val inputBitmap =
            Bitmap.createScaledBitmap(image, width, height, false)
        // 创建一张渲染后的输出图片。
        val outputBitmap =
            Bitmap.createBitmap(inputBitmap)

        // 创建RenderScript内核对象
        val rs: RenderScript = RenderScript.create(context)
        // 创建一个模糊效果的RenderScript的工具对象
        val blurScript: ScriptIntrinsicBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))

        // 由于RenderScript并没有使用VM来分配内存,所以需要使用Allocation类来创建和分配内存空间。
        // 创建Allocation对象的时候其实内存是空的,需要使用copyTo()将数据填充进去。
        val tmpIn: Allocation = Allocation.createFromBitmap(rs, inputBitmap)
        val tmpOut: Allocation = Allocation.createFromBitmap(rs, outputBitmap)

        // 设置渲染的模糊程度, 25f是最大模糊度
        blurScript.setRadius(blurRadius)
        // 设置blurScript对象的输入内存
        blurScript.setInput(tmpIn)
        // 将输出数据保存到输出内存中
        blurScript.forEach(tmpOut)

        // 将数据填充到Allocation中
        tmpOut.copyTo(outputBitmap)
        return outputBitmap
    }

    /**
     * 验证图片是否合法：比如是否太小
     * @param bmPath 图片文件路径
     * @return 是否合法
     */
    fun validate(bmPath: String): Boolean {
        val opts = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(bmPath, opts)
        // 最小边不能小于300px
        if (min(opts.outWidth, opts.outHeight) < 300) {
            return false
        }
        return true
    }

    /**
     * [adjust1]的异步方法
     */
    fun adjustAsyn(
        bmPath: String,
        minScaleSize: Int,
        onSuccess: (adjust: String) -> Unit
    ) {
        thread {
            val result = adjust1(bmPath, minScaleSize)

            Handler(Looper.getMainLooper()).post {
                onSuccess(result)
            }

        }
    }

    /**
     * 调整图片（使用调整分辨率进行加载,且调整时不伤害原图）
     * @param bmPath 图片文件路径
     * @param minScaleSize 缩放后最小的边的长度
     * @return 是否调整
     *
     */
    private fun adjust1(bmPath: String, minScaleSize: Int): String {

        var result = ""

        // 处理部分手机（三星）图片带了角度
        val bitmapDegree = getBitmapDegree(bmPath)

        val opts = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(bmPath, opts)
        if (min(opts.outWidth, opts.outHeight) > minScaleSize) {

            val minSide = min(opts.outWidth, opts.outHeight)
            val sampleSize = minSide / minScaleSize // 先算出近似缩放，避免用超大图oom

            BitmapFactory.decodeFile(bmPath, BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            })?.let { bitmap ->
                val scaleBm = if (opts.outWidth < opts.outHeight) {

                    val dstWidth = minScaleSize
                    val dstHeight = (1.0f * minScaleSize * opts.outHeight / opts.outWidth).toInt()

                    createScaleBitmap(bitmap, dstWidth, dstHeight, bitmapDegree, true)
                } else {

                    val dstWidth = (1.0f * minScaleSize * opts.outWidth / opts.outHeight).toInt()
                    val dstHeight = minScaleSize

                    createScaleBitmap(bitmap, dstWidth, dstHeight, bitmapDegree, true)
                }
                var output: FileOutputStream? = null
                try {
                    // 为了不伤到原图
                    result =
                        FileUtil.getCachePath() + File.separator + "crop_" +
                                (System.currentTimeMillis().toString() +
                                        getFileExtension(File(bmPath)))

                    output = FileOutputStream(result)
                    scaleBm.compress(Bitmap.CompressFormat.JPEG, 100, output)
                    output.flush()
                    output.close()
                } catch (e: Exception) {
                    result = ""
                } finally {
                    try {
                        output?.flush()
                        output?.close()
                    } catch (e: Exception) {

                    }
                }
                scaleBm.recycle()
                bitmap.recycle()
                return result
            }
        }
        return result
    }

    private fun getFileExtension(file: File?): String {
        var extension = ""
        try {
            if (file != null && file.exists()) {
                val name = file.name
                extension = name.substring(name.lastIndexOf("."))
            }
        } catch (e: Exception) {
            extension = ""
        }

        return extension
    }

    /**
     * 调整图片（使用Bitmap.Config.RGB_565进行加载，调整时可能会伤害原图）
     * @param bmPath 图片文件路径
     * @param minScaleSize 缩放后最小的边的长度
     * @return 是否调整
     *
     */
    fun adjust(bmPath: String, minScaleSize: Int): Boolean {

        // 处理部分手机（三星）图片带了角度
        val bitmapDegree = getBitmapDegree(bmPath)

        val opts = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(bmPath, opts)
        if (min(opts.outWidth, opts.outHeight) > minScaleSize) {

            BitmapFactory.decodeFile(bmPath, BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.RGB_565
            })?.let { bitmap ->
                val scaleBm = if (opts.outWidth < opts.outHeight) {

                    val dstWidth = minScaleSize
                    val dstHeight = (1.0f * minScaleSize * opts.outHeight / opts.outWidth).toInt()

                    createScaleBitmap(bitmap, dstWidth, dstHeight, bitmapDegree, true)
                } else {

                    val dstWidth = (1.0f * minScaleSize * opts.outWidth / opts.outHeight).toInt()
                    val dstHeight = minScaleSize

                    createScaleBitmap(bitmap, dstWidth, dstHeight, bitmapDegree, true)
                }
                var output: FileOutputStream? = null
                try {
                    output = FileOutputStream(bmPath)
                    scaleBm.compress(Bitmap.CompressFormat.JPEG, 100, output)
                    output.flush()
                    output.close()
                } catch (e: Exception) {

                } finally {
                    try {
                        output?.flush()
                        output?.close()
                    } catch (e: Exception) {

                    }
                }
                scaleBm.recycle()
                bitmap.recycle()
                return true
            }
        }
        return false
    }

    private fun createScaleBitmap(
        bitmap: Bitmap,
        dstWidth: Int,
        dstHeight: Int,
        degree: Int,
        filter: Boolean
    ): Bitmap {
        val m = Matrix()

        val width = bitmap.getWidth()
        val height = bitmap.getHeight()
        if (width != dstWidth || height != dstHeight) {
            val sx = dstWidth / width.toFloat()
            val sy = dstHeight / height.toFloat()
            m.setScale(sx, sy)
        }

        m.postRotate(degree * 1f)

        return Bitmap.createBitmap(bitmap, 0, 0, width, height, m, filter)
    }

    /**
     * 获取图片宽高
     * @param path 图片路劲
     * @return 图片的size [Size]
     */
    fun getImageWH(path: String): Size {
        val option = BitmapFactory.Options()
        option.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, option)

        val degree = getBitmapDegree(path)
        if (degree == 90 || degree == 270) {
            return Size(option.outHeight, option.outWidth)
        } else {
            return Size(option.outWidth, option.outHeight)
        }
    }

    /**
     * 获取图片旋转角度
     * @param path 图片路劲
     * @return 图片的角度
     */
    fun getBitmapDegree(path: String): Int {
        var degree = 0
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            val exifInterface = ExifInterface(path)
            // 获取图片的旋转信息
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return degree
    }

    /**
     * Bitmap保存成File
     * @param bitmap input bitmap
     * @param des output file's path
     *
     * @return String output file's path
     */
    fun bitmap2File(bitmap: Bitmap, des: String): String {
        val f = File(des)
        if (f.exists()) f.delete()
        var fOut: FileOutputStream? = null
        try {
            fOut = FileOutputStream(f)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
            fOut.flush()
            fOut.close()
        } catch (e: IOException) {
            return f.absolutePath
        } finally {
            fOut?.close()
        }
        return f.absolutePath
    }

    data class Size(val width: Int, val height: Int)
}