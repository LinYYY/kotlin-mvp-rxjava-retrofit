package com.tech.mvpframework.view.xrecyclerview

import android.view.View

/**
 */
interface ILoadingMoreFooter {

    annotation class State {
        companion object {
            const val STATE_NORMAL = 0
            const val STATE_RELEASE_TO_LOAD = 1    // 释放准备刷新（过了准备上拉的高度）
            const val STATE_LOADING = 2
            const val STATE_COMPLETE = 3
            const val STATE_NO_MORE = -1
            const val STATE_LOAD_ERROR = -2
        }
    }

    fun startLoadingMore()

    fun loadingMoreCompete()

    fun loadingNoMore()

    fun loadError()

    fun inLoading(): Boolean

    fun getView(): View

    fun onMove(delta: Float)       // 滑动了多少

    fun releaseAction(): Boolean        // 结束触摸动作

    fun getVisibleHeight(): Int

    fun getCriticalViewHeight(): Int
}