package com.tech.mvpframework.view.xrecyclerview

import android.animation.ValueAnimator
import android.content.Context
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

/**
 * [XRecyclerView]的下拉刷新基类
 */
abstract class BaseRefreshHeader(c: Context) : FrameLayout(c), IRefreshHeader {
    var mState = IRefreshHeader.State.STATE_NORMAL
        private set

    private var mContainer: FrameLayout

    init {
        // 根布局设置高度为0
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
        // 留给外部的接口
        mContainer = FrameLayout(context)
        val fl = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100)
        fl.gravity = Gravity.BOTTOM
        addView(mContainer, fl)
        initView(mContainer)
    }

    abstract fun initView(container: FrameLayout)

    /**
     * @param LastState 上一个状态
     * @param CurState  当前状态
     */
    abstract fun setViewByState(
        @IRefreshHeader.State LastState: Int,
        @IRefreshHeader.State CurState: Int
    )

    /**
     * 是否到达下拉刷新的高度
     *
     * @return
     */
    abstract fun getCriticalViewHeight(): Int

    override fun onMove(delta: Float) {
        if (getVisibleHeight() > 0 || delta > 0) {
            setVisibleHeight((getVisibleHeight() + delta).toInt())
            // 如果在非更新状态
            if (mState < IRefreshHeader.State.STATE_REFRESHING) {
                if (getVisibleHeight() < getCriticalViewHeight()) {
                    setState(IRefreshHeader.State.STATE_NORMAL)
                } else {
                    setState(IRefreshHeader.State.STATE_RELEASE_TO_REFRESH)
                }
            }
        }
    }

    override fun releaseAction(): Boolean {
        if (mState == IRefreshHeader.State.STATE_NORMAL) {
            smoothScrollTo(0)
            return false
        }
        if (mState == IRefreshHeader.State.STATE_RELEASE_TO_REFRESH) {
            smoothScrollTo(getCriticalViewHeight())
            setState(IRefreshHeader.State.STATE_REFRESHING)
            return true
        }
        if (mState == IRefreshHeader.State.STATE_REFRESHING) {
            smoothScrollTo(getCriticalViewHeight())
            return false
        }
        return if (mState == IRefreshHeader.State.STATE_DONE) {
            false
        } else false

    }

    override fun refreshComplete() {
        setState(IRefreshHeader.State.STATE_DONE)
        smoothScrollTo(0)
        Handler().postDelayed({ setState(IRefreshHeader.State.STATE_NORMAL) }, 400)
    }

    override fun getView(): View {
        return this
    }

    /**
     * 分配状态
     *
     * @param state
     */
     private fun setState(@IRefreshHeader.State state: Int) {
        when (state) {
            IRefreshHeader.State.STATE_NORMAL -> setViewByState(mState, state)
            IRefreshHeader.State.STATE_RELEASE_TO_REFRESH -> setViewByState(mState, state)
            IRefreshHeader.State.STATE_REFRESHING -> setViewByState(mState, state)
            IRefreshHeader.State.STATE_DONE -> setViewByState(mState, state)
        }
        mState = state
    }

    fun resetState() {
        setState(mState)
    }

    /**
     * 用于从当前状态滑动都destHeight
     *
     * @param destHeight
     */
    private fun smoothScrollTo(destHeight: Int) {
        val animator = ValueAnimator.ofInt(getVisibleHeight(), destHeight)
        animator.setDuration(300).start()
        animator.addUpdateListener { animation ->
            //最后地往回收view
            setVisibleHeight(animation.animatedValue as Int)
        }
        animator.start()
    }

    private fun setVisibleHeight(height: Int) {
        var height = height
        if (height < 0) height = 0
        val lp = layoutParams
        lp.height = height
        layoutParams = lp
    }

    override fun getVisibleHeight(): Int {
        val layoutParams = layoutParams
        return layoutParams.height
    }
}