package com.tech.mvpframework.base

import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 *  create by Myking
 *  date : 2020/5/14 15:44
 *  description :
 */
abstract class BaseModel : IBaseModel, LifecycleObserver {

    private var compositeDisposable: CompositeDisposable? = null

    @CallSuper
    override fun addDisposable(disposable: Disposable?) {
        if (compositeDisposable == null) {
            compositeDisposable = CompositeDisposable()
        }
        disposable?.let { compositeDisposable?.add(it) }
    }

    @CallSuper
    override fun onDetach() {
        unDispose()
    }

    private fun unDispose() {
        compositeDisposable?.clear()
        compositeDisposable = null
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    internal fun onDestroy(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
    }
}