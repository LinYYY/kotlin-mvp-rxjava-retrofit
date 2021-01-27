package com.tech.mvpframework.rx.obserber

import com.tech.mvpframework.base.IBaseView
import com.tech.mvpframework.network.entity.BaseResponse
import com.tech.mvpframework.network.entity.ErrorStatus
import io.reactivex.observers.ResourceObserver

/**
 *  create by Myking
 *  date : 2020/5/14 16:33
 *  description :观察者基类 用于Observable
 */
abstract class BaseObserver<T> : ResourceObserver<BaseResponse<T>> {

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
    protected abstract fun onSuccess(t: T?)

    /**
     * 错误的回调
     */
    protected fun onError(code: Int, msg: String) {}

    override fun onStart() {
        super.onStart()
        if (bShowLoading) view?.showLoading()
    }

    override fun onComplete() {
        view?.hideLoading()
    }

    override fun onNext(t: BaseResponse<T>) {
        view?.hideLoading()
        when (t.errorCode) {
            ErrorStatus.SUCCESS -> onSuccess(t.data)
            ErrorStatus.LOGIN_EXPIRE -> {
                view?.showErrorMsg(t.errorMsg)
                view?.loginExpire()
            }
            else -> {
                onError(t.errorCode, t.errorMsg)
                if (t.errorMsg.isNotEmpty())
                    view?.showErrorMsg(t.errorMsg)
            }
        }
    }

    override fun onError(e: Throwable) {
        view?.hideLoading()
        view?.showErrorMsg(e.message.toString())
        onError(ErrorStatus.UNKNOWN_EXCEPTION, e.message.toString())
    }
}