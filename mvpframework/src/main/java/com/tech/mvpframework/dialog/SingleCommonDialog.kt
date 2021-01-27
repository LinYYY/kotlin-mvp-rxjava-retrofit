package com.tech.mvpframework.dialog

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDialog
import com.tech.mvpframework.R
import kotlinx.android.synthetic.main.dialog_single_common.*

/**
 *  create by Myking
 *  date : 2020/6/17 17:00
 *  description : 单按钮
 */
class SingleCommonDialog(context: Context) : AppCompatDialog(context, R.style.DefaultDialogTheme) {
    var onOk: (() -> Unit)? = null

    var autoDismiss: Boolean = true

    fun setOkText(text: String) {
        btn_ok.text = text
    }

    fun setOkText(@StringRes stringId: Int) {
        btn_ok.setText(stringId)
    }

    fun setTitleText(@StringRes stringId: Int) {
        tv_title.visibility = View.VISIBLE
        tv_title.setText(stringId)
    }

    fun setTitleText(text: String) {
        tv_title.visibility = View.VISIBLE
        tv_title.text = text
    }

    fun setMessageText(@StringRes stringId: Int) {
        tv_message.visibility = View.VISIBLE
        tv_message.setText(stringId)
    }

    fun setMessageText(text: String) {
        tv_message.visibility = View.VISIBLE
        tv_message.text = text
    }

    init {
        val lp = window?.attributes
        lp?.width = ViewGroup.LayoutParams.MATCH_PARENT
        lp?.height = ViewGroup.LayoutParams.MATCH_PARENT
        window?.attributes = lp
        window?.setBackgroundDrawable(ColorDrawable())

        setCanceledOnTouchOutside(false)

        setContentView(R.layout.dialog_single_common)

        btn_ok.setOnClickListener {
            onOk?.invoke()
            if (autoDismiss) {
                dismiss()
            }
        }

    }

    override fun onBackPressed() {

    }

    override fun show() {
        if (context is Activity && ((context as Activity).isFinishing || (context as Activity).isDestroyed)) {
            return
        }
        super.show()
    }

}