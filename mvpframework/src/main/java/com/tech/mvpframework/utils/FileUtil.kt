package com.tech.mvpframework.utils

import com.tech.mvpframework.base.BaseApplication
import java.io.File

/**
 *  create by Myking
 *  date : 2020/5/14 19:20
 *  description :
 */

object FileUtil {
    private var sBaseFilePath: String? = null

    /**
     * 根目录的路径
     */
    fun getsBaseFilePath(): String? {
        return sBaseFilePath
    }

    /**
     * 日志文件路径
     */
    fun getLogFilePath(): String {
        val result = sBaseFilePath + File.separator + ".Log"
        val mkdir = File(result).mkdir()
        return result
    }

    /**
     * 缓存路径
     */
    fun getCachePath(): String {
        val result = sBaseFilePath + File.separator + ".Cache"
        File(result).mkdirs()
        return result
    }

    // 仅启动清理一次
    private var isClean = false

    /**
     * 清除缓存文件夹下的文件
     */
    fun cleanCachePath() {

        // 可能会多次经过闪屏
        if (isClean) return
        isClean = true

        val result = sBaseFilePath + File.separator + ".Cache"
        deleteDirWithFile(File(result))
    }


    private fun deleteDirWithFile(dir: File) {
        if (!dir.exists() || !dir.isDirectory())
            return
        for (file in dir.listFiles()) {
            if (file.isFile())
                file.delete() // 删除所有文件
            else if (file.isDirectory())
                deleteDirWithFile(file) // 递规的方式删除文件夹
        }
        dir.delete()// 删除目录本身
    }


    fun init() {
        val mkdir = File(sBaseFilePath).mkdir()
    }

    init {
        sBaseFilePath = BaseApplication.context.getExternalFilesDir(null)!!.path
    }
}