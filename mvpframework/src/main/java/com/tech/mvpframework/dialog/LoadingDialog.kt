package com.tech.mvpframework.dialog

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialog
import com.tech.mvpframework.R

/**
 *  create by Myking
 *  date : 2020/6/11 10:48
 *  description :
 */
class LoadingDialog(context: Context) : AppCompatDialog(context) {

    init {
        val lp = window?.attributes
        lp?.width = ViewGroup.LayoutParams.MATCH_PARENT
        lp?.height = ViewGroup.LayoutParams.MATCH_PARENT
        window?.attributes = lp
        window?.setBackgroundDrawable(ColorDrawable())
        setCanceledOnTouchOutside(false)

        setContentView(R.layout.dialog_loading)
    }

    override fun show() {
        if (context is Activity && ((context as Activity).isFinishing || (context as Activity).isDestroyed) || isShowing) {
            return
        }
        super.show()
    }
}