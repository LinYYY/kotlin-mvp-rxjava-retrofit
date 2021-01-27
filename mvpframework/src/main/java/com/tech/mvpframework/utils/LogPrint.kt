package com.tech.mvpframework.utils

import android.util.Log
import com.tech.mvpframework.BuildConfig
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 *  create by Myking
 *  date : 2020/5/14 19:18
 *  description :
 */
class LogPrint {
    companion object {

        private val LOG_FILE: String =
            FileUtil.getLogFilePath() + File.separator + "log_" + System.currentTimeMillis() + ".txt"
        private val logFileEnable = BuildConfig.DEBUG

        @JvmStatic
        fun d(tag: String, msg: String) {
            d(tag, msg, false)
        }

        @JvmStatic
        fun d(tag: String?, msg: String, write: Boolean) {
            if (BuildConfig.isDev) {
                Log.d(tag, msg)
            }
            if (write) {
                write("DEBUG", getPrefixName(), msg)
            }
        }

        @JvmStatic
        fun e(tag: String, msg: String) {
            e(tag, msg, false)
        }

        @JvmStatic
        fun e(tag: String?, msg: String, write: Boolean) {
            if (BuildConfig.isDev) {
                Log.e(tag, msg)
            }
            if (write) {
                write("ERROR", getPrefixName(), msg)
            }
        }

        /**
         * 写到文件中的log的前缀，如果因为混淆之类的原因而取不到，就返回"[ minify ]"
         *
         * @return prefix
         */
        private fun getPrefixName(): String {
            val sts =
                Thread.currentThread().stackTrace
            if (sts == null || sts.size == 0) {
                return "[ minify ]"
            }
            try {
                for (st in sts) {
                    if (st.isNativeMethod) {
                        continue
                    }
                    if (st.className == Thread::class.java.name) {
                        continue
                    }
                    if (st.className == LogPrint::class.java.name) {
                        continue
                    }
                    if (st.fileName != null) {
                        return "[ " + Thread.currentThread().name +
                                ": " + st.fileName + ":" + st.lineNumber +
                                " " + st.methodName + " ]"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return "[ minify ]"
        }

        /**
         * 追加文件：使用FileWriter
         *
         * @param level   等级
         * @param prefix  前缀
         * @param content 内容
         */
        private fun write(
            level: String,
            prefix: String,
            content: String
        ) {
            if (!logFileEnable) return
            try {
                // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
                val writer = FileWriter(LOG_FILE, true)
                val sdf =
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val time = sdf.format(Date())
                writer.write("$time: $level/$prefix: $content\n")
                writer.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}