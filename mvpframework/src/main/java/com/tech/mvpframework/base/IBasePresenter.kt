package com.tech.mvpframework.base

import android.content.Intent

/**
 *  create by Myking
 *  date : 2020/5/14 15:04
 *  description :
 */
interface IBasePresenter<in V : IBaseView> {

    fun attachView(view: V)
    fun detachView()

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
}