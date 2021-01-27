package com.tech.mvpframework.base

import android.content.Intent
import android.os.Bundle
import androidx.annotation.CallSuper

/**
 *  create by Myking
 *  date : 2020/5/14 15:31
 *  description :
 */
@Suppress("UNCHECKED_CAST")
abstract class BaseMvpActivity<in V : IBaseView, P : IBasePresenter<V>> : BaseActivity() {

    var presenter: P? = null

    protected abstract fun createPresenter(): P?

    @CallSuper
    override fun initView(savedInstanceState: Bundle?) {
        presenter = createPresenter()
        presenter?.attachView(this as V)
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        presenter?.detachView()
        presenter = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presenter?.onActivityResult(requestCode, resultCode, data)
    }
}