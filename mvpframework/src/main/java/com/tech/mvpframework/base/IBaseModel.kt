package com.tech.mvpframework.base

import io.reactivex.disposables.Disposable

/**
 *  create by Myking
 *  date : 2020/5/14 15:06
 *  description :
 */
interface IBaseModel {

    fun addDisposable(disposable: Disposable?)

    fun onDetach()
}