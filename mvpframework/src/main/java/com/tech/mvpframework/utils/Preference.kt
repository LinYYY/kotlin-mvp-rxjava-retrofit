package com.tech.mvpframework.utils

import android.content.Context
import android.content.SharedPreferences
import com.tech.mvpframework.base.BaseApplication
import com.tech.mvpframework.constant.Constant.SP_NAME
import com.tech.mvpframework.constant.SPKey
import java.io.*
import kotlin.reflect.KProperty

/**
 *  create by Myking
 *  date : 2020/5/14 17:20
 *  description :kotlin委托属性+SharedPreference实例
 */
class Preference<T>(var name: String, private var default: T) {

    companion object {

        private val sp: SharedPreferences by lazy {
            BaseApplication.context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        }

        /**
         * 删除全部数据
         */
        @JvmStatic
        fun clearPreference() {
            sp.edit().clear().apply()
        }

        /**
         * 根据key删除存储数据
         */
        @JvmStatic
        fun clearPreference(key: String) {
            sp.edit().remove(key).apply()
        }

        /**
         * 查询某个key是否已经存在
         *
         * @param key
         * @return
         */
        @JvmStatic
        fun contains(key: String): Boolean {
            return sp.contains(key)
        }

        /**
         * 返回所有的键值对
         *
         * @return
         */
        @JvmStatic
        fun getAll(): Map<String, *> {
            return sp.all
        }
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return getSp(name, default)
    }


    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        putSp(name, value)
    }


    fun putSp(name: String, value: T) = with(sp.edit()) {
        when (value) {
            is Long -> putLong(name, value)
            is String -> putString(name, value)
            is Int -> putInt(name, value)
            is Boolean -> putBoolean(name, value)
            is Float -> putFloat(name, value)
            else -> putString(name, serialize(value))
        }.apply()
    }

    fun getSp(name: String, default: T): T = with(sp) {
        when (default) {
            is Long -> getLong(name, default)
            is String -> getString(name, default)
            is Int -> getInt(name, default)
            is Boolean -> getBoolean(name, default)
            is Float -> getFloat(name, default)
            else -> deSerialization(getString(name, serialize(default)) ?: "")
        } as T
    }

    /**
     * 序列化对象

     * @param obj
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun <A> serialize(obj: A): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(
            byteArrayOutputStream
        )
        objectOutputStream.writeObject(obj)
        var serStr = byteArrayOutputStream.toString("ISO-8859-1")
        serStr = java.net.URLEncoder.encode(serStr, "UTF-8")
        objectOutputStream.close()
        byteArrayOutputStream.close()
        return serStr
    }

    /**
     * 反序列化对象

     * @param str
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IOException::class, ClassNotFoundException::class)
    private fun <A> deSerialization(str: String): A {
        val redStr = java.net.URLDecoder.decode(str, "UTF-8")
        val byteArrayInputStream = ByteArrayInputStream(
            redStr.toByteArray(charset("ISO-8859-1"))
        )
        val objectInputStream = ObjectInputStream(
            byteArrayInputStream
        )
        val obj = objectInputStream.readObject() as A
        objectInputStream.close()
        byteArrayInputStream.close()
        return obj
    }
}