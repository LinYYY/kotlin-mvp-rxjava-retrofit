package com.tech.mvpframework.base

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDexApplication
import kotlin.properties.Delegates

/**
 *  create by Myking
 *  date : 2020/5/14 16:56
 *  description :
 */
abstract class BaseApplication : MultiDexApplication() {

    companion object {
        @JvmStatic
        lateinit var instance: Application

        @JvmStatic
        var context: Context by Delegates.notNull()
            private set

    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        context = applicationContext
    }

}