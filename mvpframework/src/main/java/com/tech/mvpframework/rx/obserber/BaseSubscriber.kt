package com.tech.mvpframework.rx.obserber

import com.tech.mvpframework.base.IBaseView
import io.reactivex.subscribers.ResourceSubscriber

/**
 *  create by Myking
 *  date : 2020/5/15 13:41
 *  description :观察者基类 用于Flowable
 */
abstract class BaseSubscriber<T> : ResourceSubscriber<T> {
    private var view: IBaseView? = null
    private var bShowLoading = true

    constructor(view: IBaseView) {
        this.view = view
    }

    constructor(view: IBaseView, bShowLoading: Boolean) {
        this.view = view
        this.bShowLoading = bShowLoading
    }

    /**
     * 成功的回调
     */
    protected abstract fun onSuccess(t: T)

    /**
     * 错误的回调
     */
    protected fun onError(msg: String) {}

    override fun onStart() {
        super.onStart()
        if (bShowLoading) view?.showLoading()
    }

    override fun onComplete() {
        view?.hideLoading()
    }

    override fun onNext(t: T) {
        view?.hideLoading()
        onSuccess(t)
    }

    override fun onError(e: Throwable) {
        view?.hideLoading()
        view?.showErrorMsg(e.message.toString())
        onError(e.message.toString())
    }
}