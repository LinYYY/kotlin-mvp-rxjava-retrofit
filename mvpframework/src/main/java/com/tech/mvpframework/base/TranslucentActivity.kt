package com.tech.mvpframework.base

import android.os.Bundle
import android.view.Window
import android.view.WindowManager

/**
 * @author William
 * @description: 半透明全屏activity,注意要在manifest中设置style为ActivityAsDialog
 * @date :2020-09-16
 */
abstract class TranslucentActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)//取消标题栏
        window.decorView.setPadding(0, 0, 0, 0)
        val layoutParams = window.attributes
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        window.attributes = layoutParams
        super.onCreate(savedInstanceState)
    }
}