package com.tech.mvpframework.dialog

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDialog
import com.tech.mvpframework.R
import com.tech.mvpframework.utils.color
import kotlinx.android.synthetic.main.dialog_common.*

/**
 *  create by Myking
 *  date : 2020/6/8 15:56
 *  description : 双按钮
 */
class CommonDialog(context: Context) : AppCompatDialog(context, R.style.DefaultDialogTheme) {

    var onCancel: (() -> Unit)? = null
    var onOk: (() -> Unit)? = null

    fun setCancelText(text: String) {
        btn_cancel.text = text
    }

    fun setCancelText(@StringRes stringId: Int) {
        btn_cancel.setText(stringId)
    }

    fun setCancelTextColor(@ColorRes colorId: Int) {
        btn_cancel.color(colorId)
    }

    fun setCancelBackground(@DrawableRes drawableId: Int) {
        btn_cancel.setBackgroundResource(drawableId)
    }

    fun setCancelBackground(drawable: Drawable) {
        btn_cancel.background = drawable
    }

    fun setOkText(text: String) {
        btn_ok.text = text
    }

    fun setOkText(@StringRes stringId: Int) {
        btn_ok.setText(stringId)
    }

    fun setOkTextColor(@ColorRes colorId: Int) {
        btn_ok.color(colorId)
    }

    fun setOkBackground(@DrawableRes drawableId: Int) {
        btn_ok.setBackgroundResource(drawableId)
    }

    fun setOkBackground(drawable: Drawable) {
        btn_ok.background = drawable
    }

    fun setTitleText(@StringRes stringId: Int) {
        tv_title.setText(stringId)
        tv_title.visibility = View.VISIBLE
    }

    fun setTitleText(text: String) {
        tv_title.text = text
        tv_title.visibility = View.VISIBLE
    }

    fun setMessageText(@StringRes stringId: Int) {
        tv_message.setText(stringId)
        tv_message.visibility = View.VISIBLE
    }

    fun setMessageText(text: String) {
        tv_message.text = text
        tv_message.visibility = View.VISIBLE
    }

    init {
        val lp = window?.attributes
        lp?.width = ViewGroup.LayoutParams.MATCH_PARENT
        lp?.height = ViewGroup.LayoutParams.MATCH_PARENT

        window?.attributes = lp
        window?.setBackgroundDrawable(ColorDrawable())

        setCanceledOnTouchOutside(false)

        setContentView(R.layout.dialog_common)

        btn_ok.setOnClickListener {
            onOk?.invoke()
            dismiss()
        }

        btn_cancel.setOnClickListener {
            onCancel?.invoke()
            dismiss()
        }
    }

    override fun show() {
        if (context is Activity && ((context as Activity).isFinishing || (context as Activity).isDestroyed)) {
            return
        }
        super.show()
    }
}