package com.tech.mvpframework.rx

import com.tech.mvpframework.R
import com.tech.mvpframework.base.BaseApplication
import com.tech.mvpframework.base.IBaseModel
import com.tech.mvpframework.base.IBaseView
import com.tech.mvpframework.network.entity.BaseResponse
import com.tech.mvpframework.network.entity.ErrorStatus
import com.tech.mvpframework.network.funtion.RetryWithDelay
import com.tech.mvpframework.network.funtion.RetryWithDelayPublish
import com.tech.mvpframework.network.utils.NetWorkUtil
import com.tech.mvpframework.utils.loge
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import retrofit2.HttpException

/**
 *  create by Myking
 *  date : 2020/5/14 17:20
 *  description : rx操作扩展
 */

/********* 网络请求 Start *******/
fun <T> Observable<BaseResponse<T>>.ss(
    model: IBaseModel?,
    view: IBaseView?,
    isShowLoading: Boolean = true,
    success: (T?) -> Unit,
    error: ((code: Int, msg: String) -> Unit)? = null
) {
    this.compose(SchedulerUtil.ioMainScheduler())
        .retryWhen(RetryWithDelay())
        .subscribe(object : Observer<BaseResponse<T>> {
            override fun onComplete() {
            }

            override fun onSubscribe(d: Disposable) {
                if (isShowLoading) view?.showLoading()
                model?.addDisposable(d)
                if (!NetWorkUtil.isNetworkConnected(BaseApplication.instance)) {
                    loge("Network Unavailable!!!")
                    view?.showNetworkError()
//                    view?.showErrorMsg("Network Unavailable!!!")
                    error?.invoke(ErrorStatus.UNKNOWN_EXCEPTION, "Network Unavailable!!!")
                    onComplete()
                }
            }

            override fun onNext(t: BaseResponse<T>) {
                view?.hideLoading()
                when (t.errorCode) {
                    ErrorStatus.SUCCESS -> success.invoke(t.data)
                    ErrorStatus.LOGIN_EXPIRE, ErrorStatus.NO_LOGIN -> {
                        view?.showErrorMsg(t.errorMsg)
                        view?.loginExpire()
                        error?.invoke(t.errorCode, t.errorMsg)
                    }
                    ErrorStatus.NEED_UPDATE -> {
                        view?.needUpdate(t.errorMsg)
                        error?.invoke(t.errorCode, t.errorMsg)
                    }
                    else -> {
                        loge(t.errorMsg)
                        view?.showErrorMsg(t.errorMsg)
                        error?.invoke(t.errorCode, t.errorMsg)
                    }
                }
            }

            override fun onError(t: Throwable) {
//                loge(t.message)
                t.printStackTrace()
                view?.hideLoading()
                view?.showErrorMsg(t.message.toString())
                error?.invoke(ErrorStatus.UNKNOWN_EXCEPTION, t.message.toString())
            }
        })
}

fun <T> Observable<BaseResponse<T>>.sss(
    view: IBaseView?,
    isShowLoading: Boolean = true,
    onSuccess: (T?) -> Unit,
    onError: ((code: Int, msg: String) -> Unit)? = null
): Disposable {
    if (isShowLoading) view?.showLoading()
    return this.compose(SchedulerUtil.ioMainScheduler())
        .retryWhen(RetryWithDelay())
        .subscribe({
            when (it.errorCode) {
                ErrorStatus.SUCCESS -> onSuccess.invoke(it.data)
                ErrorStatus.LOGIN_EXPIRE, ErrorStatus.NO_LOGIN -> {
                    view?.showErrorMsg(it.errorMsg)
                    view?.loginExpire()
                    onError?.invoke(it.errorCode, it.errorMsg)
                }
                ErrorStatus.NEED_UPDATE -> {
                    view?.needUpdate(it.errorMsg)
                    onError?.invoke(it.errorCode, it.errorMsg)
                }
                else -> {
                    view?.showErrorMsg(it.errorMsg)
                    loge(it.errorMsg)
                    onError?.invoke(it.errorCode, it.errorMsg)
                }
            }
            view?.hideLoading()
        }, {
            it.printStackTrace()
            view?.hideLoading()
            view?.showErrorMsg(it.message.toString())
            onError?.invoke(ErrorStatus.UNKNOWN_EXCEPTION, it.message.toString())
        })
}

fun <T> Single<T>.sss(
    view: IBaseView?,
    isShowLoading: Boolean = true,
    onSuccess: (T) -> Unit,
    onError: ((msg: String) -> Unit)? = null
): Disposable {
    if (isShowLoading) view?.showLoading()
    return this.compose(SchedulerUtil.ioMainScheduler())
        .retryWhen(RetryWithDelayPublish())
        .subscribe({
            view?.hideLoading()
            onSuccess(it)
        }, {
            val msg = it.message ?: "unknown error"
            loge(msg)
            view?.hideLoading()
            view?.showErrorMsg(msg)
            onError?.invoke(msg)
            if (it is HttpException && it.code() == 401) {
                // 用户登录过期
                view?.showErrorMsg(BaseApplication.context.getString(R.string.login_expired_toast))
            }
        })
}
/********* 网络请求 End *******/


/*** 数据库io操作 Start *****/
fun <T> Flowable<T>.io(
    view: IBaseView?,
    isShowLoading: Boolean = true,
    onSuccess: (T) -> Unit,
    onError: ((msg: String) -> Unit)? = null
): Disposable {
    if (isShowLoading) view?.showLoading()
    return this.compose(SchedulerUtil.ioMainScheduler())
        .subscribe({
            onSuccess(it)
            view?.hideLoading()
        }, {
            loge(it.message)
            view?.hideLoading()
            view?.showErrorMsg(it.message.toString())
            onError?.invoke(it.message.toString())
        })
}

fun <T> Single<T>.io(
    view: IBaseView?,
    isShowLoading: Boolean = true,
    onSuccess: (T) -> Unit,
    onError: ((msg: String) -> Unit)? = null
): Disposable {
    if (isShowLoading) view?.showLoading()
    return this.compose(SchedulerUtil.ioMainScheduler())
        .subscribe({
            onSuccess(it)
            view?.hideLoading()
        }, {
            val msg = it.message ?: "unknown error"
            loge(msg)
            view?.hideLoading()
            view?.showErrorMsg(msg)
            onError?.invoke(msg)
        })
}
/*** 数据库io操作 End *****/
