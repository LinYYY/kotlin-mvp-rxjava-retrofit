package com.tech.mvpframework.base

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

/**
 *  create by Myking
 *  date : 2020/5/14 15:01
 *  description :
 */
interface IBaseView {

    fun showLoading()
    fun hideLoading()

    fun showToast(msg: String, during: Int = Toast.LENGTH_SHORT)
    fun showToast(@StringRes stringId: Int, during: Int = Toast.LENGTH_SHORT)

    fun showErrorMsg(errorMsg: String)

    fun showNetworkError()

    fun loginExpire()
    fun needUpdate(msg: String)

    fun getActivityContext(): Activity?
}