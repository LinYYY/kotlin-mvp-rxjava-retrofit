package com.tech.mvpframework.base

import android.view.View
import androidx.annotation.CallSuper

/**
 *  create by Myking
 *  date : 2020/5/14 15:42
 *  description :
 */
abstract class BaseMvpFragment<in V : IBaseView, P : IBasePresenter<V>> : BaseFragment() {

    protected var presenter: P? = null

    protected abstract fun createPresenter(): P?

    @CallSuper
    override fun initView(view: View) {
        presenter = createPresenter()
        presenter?.attachView(this as V)
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        presenter?.detachView()
        presenter = null
    }

}