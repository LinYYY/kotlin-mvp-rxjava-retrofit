package com.tech.mvpframework.utils

import android.content.Context
import android.content.SharedPreferences
import com.tech.mvpframework.base.BaseApplication
import com.tech.mvpframework.constant.Constant.GET_CODE_SP_NAME
import com.tech.mvpframework.constant.Constant.SP_NAME

/**
 *  create by Myking
 *  date : 2020/5/14 17:58
 *  description :
 */
object PreferenceUtil {

    private val appSp: SharedPreferences
        get() = BaseApplication.context.getSharedPreferences(GET_CODE_SP_NAME, Context.MODE_PRIVATE)

    @JvmStatic
    fun putString(key: String, value: String) {
        appSp.edit().putString(key, value).apply()
    }

    @JvmStatic
    fun putInt(key: String, value: Int) {
        appSp.edit().putInt(key, value).apply()
    }

    @JvmStatic
    fun getInt(key: String, value: Int): Int {
        return appSp.getInt(key, value)
    }

    @JvmStatic
    fun getString(key: String, defValue: String): String? {
        return appSp.getString(key, defValue)
    }

    @JvmStatic
    fun putBoolean(key: String, value: Boolean) {
        appSp.edit().putBoolean(key, value).apply()
    }

    @JvmStatic
    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return appSp.getBoolean(key, defValue)
    }

    @JvmStatic
    fun putLong(key: String, value: Long) {
        appSp.edit().putLong(key, value).apply()
    }

    @JvmStatic
    fun getLong(key: String, defValue: Long): Long {
        return appSp.getLong(key, defValue)
    }

    @JvmStatic
    fun remove(key: String) {
        appSp.edit().remove(key).apply()
    }

    /**
     * 移除所有数据
     */
    @JvmStatic
    fun removeAll() {
        appSp.edit().clear().apply()
    }

    /**
     * 移除用户所有数据,防止切换用户数据未更新
     */
    @JvmStatic
    fun removeUseData() {
    }

}