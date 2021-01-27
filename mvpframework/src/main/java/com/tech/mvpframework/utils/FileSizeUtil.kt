package com.tech.mvpframework.utils

import java.io.File
import java.io.FileInputStream
import java.text.DecimalFormat

/**
 *  create by Myking
 *  date : 2020/11/17 15:20
 *  description :
 */
object FileSizeUtil {

    private val TAG = FileSizeUtil::class.java.simpleName

    const val SIZETYPE_B = 1 //获取文件大小单位为B的double值

    const val SIZETYPE_KB = 2 //获取文件大小单位为KB的double值

    const val SIZETYPE_MB = 3 //获取文件大小单位为MB的double值

    const val SIZETYPE_GB = 4 //获取文件大小单位为GB的double值

    /**
     * 获取文件指定文件的指定单位的大小
     *
     * @param filePath 文件路径
     * @param sizeType 获取大小的类型1为B、2为KB、3为MB、4为GB
     * @return double值的大小
     */
    fun getFileOrFilesSize(filePath: String?, sizeType: Int): Double {
        val file = File(filePath)
        var blockSize: Long = 0
        try {
            blockSize = if (file.isDirectory) {
                getFileSizes(file)
            } else {
                getFileSize(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loge(TAG, "获取文件大小失败!")
        }
        return formatFileSize(blockSize, sizeType)
    }

    /**
     * 调用此方法自动计算指定文件或指定文件夹的大小
     *
     * @param filePath 文件路径
     * @return 计算好的带B、KB、MB、GB的字符串
     */
    fun getAutoFileOrFilesSize(filePath: String?): String? {
        val file = File(filePath)
        var blockSize: Long = 0
        try {
            blockSize = if (file.isDirectory) {
                getFileSizes(file)
            } else {
                getFileSize(file)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            loge(TAG, "获取文件大小失败!")
        }
        return formatFileSize(blockSize)
    }

    /**
     * 获取指定文件大小
     *
     * @param file
     * @return
     * @throws Exception
     */
    @Throws(java.lang.Exception::class)
    private fun getFileSize(file: File): Long {
        var size: Long = 0
        if (file.exists()) {
            var fis: FileInputStream? = null
            fis = FileInputStream(file)
            size = fis.available().toLong()
        } else {
            file.createNewFile()
            loge(TAG, "获取文件大小不存在!")
        }
        return size
    }

    /**
     * 获取指定文件夹
     *
     * @param f
     * @return
     * @throws Exception
     */
    @Throws(java.lang.Exception::class)
    private fun getFileSizes(f: File): Long {
        var size: Long = 0
        val flist = f.listFiles()
        for (i in flist.indices) {
            size = if (flist[i].isDirectory) {
                size + getFileSizes(flist[i])
            } else {
                size + getFileSize(flist[i])
            }
        }
        return size
    }

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    private fun formatFileSize(fileS: Long): String? {
        val df = DecimalFormat("#.00")
        var fileSizeString = ""
        val wrongSize = "0B"
        if (fileS == 0L) {
            return wrongSize
        }
        fileSizeString = when {
            fileS < 1024 -> {
                df.format(fileS.toDouble()).toString() + "B"
            }
            fileS < 1048576 -> {
                df.format(fileS.toDouble() / 1024).toString() + "KB"
            }
            fileS < 1073741824 -> {
                df.format(fileS.toDouble() / 1048576).toString() + "MB"
            }
            else -> {
                df.format(fileS.toDouble() / 1073741824).toString() + "GB"
            }
        }
        return fileSizeString
    }

    /**
     * 转换文件大小,指定转换的类型
     *
     * @param fileS
     * @param sizeType
     * @return
     */
    private fun formatFileSize(fileS: Long, sizeType: Int): Double {
        val df = DecimalFormat("#.00")
        var fileSizeLong = 0.0
        when (sizeType) {
            SIZETYPE_B -> fileSizeLong = java.lang.Double.valueOf(df.format(fileS.toDouble()))
            SIZETYPE_KB -> fileSizeLong =
                java.lang.Double.valueOf(df.format(fileS.toDouble() / 1024))
            SIZETYPE_MB -> fileSizeLong =
                java.lang.Double.valueOf(df.format(fileS.toDouble() / 1048576))
            SIZETYPE_GB -> fileSizeLong =
                java.lang.Double.valueOf(df.format(fileS.toDouble() / 1073741824))
            else -> {
            }
        }
        return fileSizeLong
    }

}