package com.tech.mvpframework.view.xrecyclerview

import android.view.View

/**
 *
 */
interface IRefreshHeader {

    annotation class State {
        companion object {
            const val STATE_NORMAL = 0                 //
            const val STATE_RELEASE_TO_REFRESH = 1     // 释放准备刷新（过了准备刷新的高度）
            const val STATE_REFRESHING = 2             // 正在刷新
            const val STATE_DONE = 3                   // 刷新结束(有结束动画)
        }
    }

    fun onMove(delta: Float)       // 滑动了多少

    fun releaseAction(): Boolean        // 结束触摸动作

    fun refreshComplete()         // 刷新完成

    fun getVisibleHeight(): Int        // 获得当前View的高度

    fun getView(): View
}