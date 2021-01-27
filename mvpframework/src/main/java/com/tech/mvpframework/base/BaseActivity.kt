package com.tech.mvpframework.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.tech.mvpframework.R
import com.tech.mvpframework.dialog.CommonDialog
import com.tech.mvpframework.dialog.LoadingDialog
import com.tech.mvpframework.dialog.SingleCommonDialog
import com.tech.mvpframework.permissions.PermissionUtil
import com.tech.mvpframework.utils.loge
import com.tech.mvpframework.view.alert.Alerter
import com.tech.mvpframework.view.alert.OnHideAlertListener
import org.greenrobot.eventbus.EventBus

/**
 *  create by Myking
 *  date : 2020/5/14 15:07
 *  description :
 */
abstract class BaseActivity : AppCompatActivity(), IBaseView {

    protected var isActivityRunning = false

    protected val networkErrorDialog: CommonDialog by lazy {
        CommonDialog(this).apply {
            setOkText("Retry")
            setCancelText(R.string.cancel)
            setMessageText(R.string.no_network)
            onOk = {
//                initData()
            }
        }
    }

    val needUpdateDialog by lazy {
        SingleCommonDialog(this).apply {
            setOkText("Update now")
            setCancelable(false)
            autoDismiss = false
            onOk = {
                // 跳转至GP下载
                val uri: Uri =
                    Uri.parse("https://play.google.com/store/apps/details?id=${packageName}")
                val intent = Intent()
                intent.action = "android.intent.action.VIEW"
                intent.data = uri
                startActivity(intent)
            }
        }
    }

    protected val loadingDialog: LoadingDialog by lazy {
        LoadingDialog(this)
    }

    /**
     * 是否使用eventbus
     */
    open fun userEventBus(): Boolean = false

    @LayoutRes
    abstract fun getLayoutId(): Int

    protected abstract fun initView(savedInstanceState: Bundle?)

    protected abstract fun initData()

    protected open fun initListener() {

    }

    protected open fun initOther() {

    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            window.statusBarColor = Color.TRANSPARENT
        }
        super.onCreate(savedInstanceState)
        if (userEventBus()) {
            EventBus.getDefault().register(this)
        }
        setContentView(getLayoutId())
        initView(savedInstanceState)
        initData()
        initListener()
        initOther()
        isActivityRunning = true
    }

    override fun onResume() {
        super.onResume()
        PermissionUtil.onResume(this)
    }

    fun showAlert(
        msg: String,
        intent: Intent? = null,
        onClickListener: View.OnClickListener? = null,
        onHideAlertListener: OnHideAlertListener? = null
    ) {
        Alerter.create(this).enableSwipeToDismiss()
            .setOnClickListener(onClickListener ?: View.OnClickListener {
                hideAlert()
                if (intent != null) {
                    startActivity(intent)
                }
            }).setOnHideListener(onHideAlertListener).setText(msg).setDuration(3000).show()
    }

    fun hideAlert() {
        Alerter.hide()
    }

    override fun onStop() {
        super.onStop()
        hideAlert()
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        isActivityRunning = false
        loadingDialog.dismiss()
        networkErrorDialog.dismiss()
        if (userEventBus()) {
            EventBus.getDefault().unregister(this)
        }
//        CommonUtils.fixInputMethodManagerLeak(this)
    }

    override fun onBackPressed() {
        ActivityCompat.finishAfterTransition(this)
    }

    /**
     * @param permission 非必须权限
     * @param mustPermission 必须权限
     */
    fun requestRuntimePermissions(
        c: Context,
        permission: ArrayList<String>,
        mustPermission: ArrayList<String>,
        listen: PermissionUtil.PermissionUtilListen
    ) {
        PermissionUtil.requestRuntimePermissions(c, permission, mustPermission, listen)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionUtil.onRequestPermissionsResult(
            this, requestCode,
            permissions as Array<String>, grantResults
        )
    }

    override fun showLoading() {
        if (isActivityRunning) {
            loadingDialog.show()
        }
    }

    override fun hideLoading() {
        loadingDialog.dismiss()
    }

    override fun showToast(msg: String, during: Int) {
        Toast.makeText(this, msg, during).show()
    }

    override fun showToast(stringId: Int, during: Int) {
        Toast.makeText(this, stringId, during).show()
    }

    override fun showErrorMsg(errorMsg: String) {
        loge(errorMsg)
        showToast(errorMsg)
    }

    override fun loginExpire() {

    }

    override fun showNetworkError() {
        if (isActivityRunning) {
            networkErrorDialog.show()
        }
    }

    override fun needUpdate(msg: String) {
        needUpdateDialog.setMessageText(msg)
        if (isActivityRunning) {
            needUpdateDialog.show()
        }
    }

    override fun getActivityContext(): Activity? {
        return this
    }

    fun showKeyboard(v: EditText) {
        val imm: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(v, 0)
    }

    fun hideKeyboard() {
        val imm: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
    }
}