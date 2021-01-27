package com.tech.mvpframework.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.tech.mvpframework.R
import com.tech.mvpframework.dialog.CommonDialog
import com.tech.mvpframework.dialog.LoadingDialog
import com.tech.mvpframework.utils.loge
import org.greenrobot.eventbus.EventBus

/**
 *  create by Myking
 *  date : 2020/5/14 15:17
 *  description :
 */
abstract class BaseFragment : Fragment(), IBaseView {

    protected val networkErrorDialog: CommonDialog by lazy {
        CommonDialog(activity!!).apply {
            setOkText("Retry")
            setCancelText(R.string.cancel)
            setMessageText(R.string.no_network)
            onOk = {
                loadData()
            }
        }
    }

    protected val loadingDialog: LoadingDialog by lazy {
        LoadingDialog(activity!!)
    }

    /**
     * 数据是否加载
     */
    protected var isDataLoaded = false

    /**
     * 视图是否创建
     */
    private var isViewPrepare = false

    /**
     * 是否使用eventbus
     */
    open fun userEventBus(): Boolean = false

    /**
     * 是否懒加载
     */
    protected open fun needLazyLoad(): Boolean = true

    @LayoutRes
    protected abstract fun getLayoutId(): Int

    protected abstract fun initView(view: View)

    protected abstract fun loadData()

    protected open fun initListener() {

    }

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(getLayoutId(), container, false)
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (userEventBus()) {
            EventBus.getDefault().register(this)
        }
        isViewPrepare = true
        initView(view)
        initListener()
        if (needLazyLoad()) {
            lazyLoadPrepare()
        } else {
            loadData()
            isDataLoaded = true
        }
    }

    @CallSuper
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            lazyLoadPrepare()
        }
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        if (userEventBus()) {
            EventBus.getDefault().unregister(this)
        }
        loadingDialog.dismiss()
        networkErrorDialog.dismiss()
    }

    override fun showLoading() {
        loadingDialog.show()
    }

    override fun hideLoading() {
        loadingDialog.dismiss()
    }

    override fun showToast(msg: String, during: Int) {
        activity?.let {
            Toast.makeText(it, msg, during).show()
        }
    }

    override fun showErrorMsg(errorMsg: String) {
        loge(errorMsg)
        showToast(errorMsg)
    }

    override fun showToast(stringId: Int, during: Int) {
        activity?.let {
            Toast.makeText(it, stringId, during).show()
        }
    }

    override fun showNetworkError() {
        networkErrorDialog.show()
    }

    override fun loginExpire() {

    }

    override fun getActivityContext(): Activity? {
        return activity
    }

    override fun needUpdate(msg: String) {
        activity?.let {
            if (it is BaseActivity) {
                it.needUpdateDialog.apply {
                    setMessageText(msg)
                    show()
                }
            }
        }
    }

    private fun lazyLoadPrepare() {
        if (userVisibleHint && isViewPrepare && !isDataLoaded) {
            loadData()
            isDataLoaded = true
        }
    }

}