package com.tech.mvpframework.base

import android.content.Intent
import androidx.annotation.CallSuper
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.greenrobot.eventbus.EventBus

/**
 *  create by Myking
 *  date : 2020/5/14 15:54
 *  description :presenter基类
 */
abstract class BasePresenter<V : IBaseView, M : IBaseModel> : IBasePresenter<V>, LifecycleObserver {

    var view: V? = null
    var model: M? = null

    private var compositeDisposable: CompositeDisposable? = null

    protected abstract fun createModel(): M?

    open fun userEventBus() = false

    @CallSuper
    override fun attachView(view: V) {
        this.view = view
        this.model = createModel()

        if (view is LifecycleOwner) {
            view.lifecycle.addObserver(this)
            if (model != null && model is LifecycleOwner) {
                view.lifecycle.addObserver(model as LifecycleObserver)
            }
        }

        if (userEventBus()) {
            EventBus.getDefault().register(this)
        }
    }

    @CallSuper
    open fun addDisposable(disposable: Disposable?) {
        if (compositeDisposable == null) {
            compositeDisposable = CompositeDisposable()
        }
        disposable?.let { compositeDisposable?.add(it) }
    }

    @CallSuper
    override fun detachView() {
        if (userEventBus()) {
            EventBus.getDefault().unregister(this)
        }
        unDispose()
        model?.onDetach()
        view = null
        model = null
    }

    private fun unDispose() {
        compositeDisposable?.clear()
        compositeDisposable = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

    }
}