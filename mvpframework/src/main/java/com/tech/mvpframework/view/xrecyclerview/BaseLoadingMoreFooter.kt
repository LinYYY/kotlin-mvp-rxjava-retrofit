package com.tech.mvpframework.view.xrecyclerview

import android.animation.ValueAnimator
import android.content.Context
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.tech.mvpframework.utils.CommonUtils
import com.tech.mvpframework.utils.logd

/**
 * [XRecyclerView]的上拉刷新基类
 */
abstract class BaseLoadingMoreFooter(c: Context) : FrameLayout(c), ILoadingMoreFooter {
    var mState = ILoadingMoreFooter.State.STATE_NORMAL
        private set
    private var mContainer: FrameLayout

    init {
        // 根布局设置高度为0
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
        // 留给外部的接口
        mContainer = FrameLayout(context)
        val fl = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100)
        fl.gravity = Gravity.TOP
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

    override fun startLoadingMore() {
        setState(ILoadingMoreFooter.State.STATE_LOADING)
        logd("aaaa", "startLoadingMore")
        smoothScrollTo(getCriticalViewHeight())
    }

    override fun loadingMoreCompete() {
        setState(ILoadingMoreFooter.State.STATE_COMPLETE)
        logd("aaaa", "loadingMoreCompete")
        smoothScrollTo(0)
        setState(ILoadingMoreFooter.State.STATE_NORMAL)
    }

    override fun loadingNoMore() {
        setState(ILoadingMoreFooter.State.STATE_NO_MORE)
        logd("aaaa", "loadingNoMore")
        smoothScrollTo(getCriticalViewHeight())
    }

    override fun loadError() {
        setState(ILoadingMoreFooter.State.STATE_LOAD_ERROR)
        logd("aaaa", "loadingError")
        smoothScrollTo(getCriticalViewHeight())
    }

    override fun inLoading(): Boolean {
        return mState == ILoadingMoreFooter.State.STATE_LOADING
    }

    override fun getView(): View {
        return this
    }

    private fun setState(@ILoadingMoreFooter.State state: Int) {
        when (state) {
            ILoadingMoreFooter.State.STATE_NORMAL -> setViewByState(mState, state)
            ILoadingMoreFooter.State.STATE_LOADING -> setViewByState(mState, state)
            ILoadingMoreFooter.State.STATE_COMPLETE -> setViewByState(mState, state)
            ILoadingMoreFooter.State.STATE_RELEASE_TO_LOAD -> setViewByState(mState, state)
            ILoadingMoreFooter.State.STATE_NO_MORE -> setViewByState(mState, state)
            ILoadingMoreFooter.State.STATE_LOAD_ERROR -> setViewByState(mState, state)
        }
        mState = state
    }

    fun resetState() {
        setState(mState)
    }

    /**
     * 是否到达下拉刷新的高度
     *
     * @return
     */
    override fun getCriticalViewHeight(): Int {
        return CommonUtils.dp2px(context, 50f)
    }

    override fun onMove(delta: Float) {
        logd("aaaa", "delta:${delta} visibleHeight:${getVisibleHeight()},当前状态:${mState}")
        if ((getVisibleHeight() >= 0 || delta < 0)) {
            setVisibleHeight((getVisibleHeight() + Math.abs(delta)).toInt())
            // 如果在非更新状态
            if (mState < ILoadingMoreFooter.State.STATE_LOADING) {
                if (mState == ILoadingMoreFooter.State.STATE_NO_MORE) {
                    setState(ILoadingMoreFooter.State.STATE_NO_MORE)
                } else if (mState == ILoadingMoreFooter.State.STATE_LOAD_ERROR) {
                    setState(ILoadingMoreFooter.State.STATE_LOAD_ERROR)
                } else if (getVisibleHeight() < getCriticalViewHeight()) {
                    setState(ILoadingMoreFooter.State.STATE_NORMAL)
                } else {
                    setState(ILoadingMoreFooter.State.STATE_RELEASE_TO_LOAD)
                }
            }
        }
    }

    override fun releaseAction(): Boolean {
        if (mState == ILoadingMoreFooter.State.STATE_NORMAL) {
            logd("aaaa", "releaseAction normal")
            smoothScrollTo(0)
            return false
        }
        if (mState == ILoadingMoreFooter.State.STATE_RELEASE_TO_LOAD) {
            logd("aaaa", "releaseAction release")
            smoothScrollTo(getCriticalViewHeight())
            setState(ILoadingMoreFooter.State.STATE_LOADING)
            return true
        }
        if (mState == ILoadingMoreFooter.State.STATE_LOADING) {
            logd("aaaa", "releaseAction loading")
            smoothScrollTo(getCriticalViewHeight())
            return false
        }
        if (mState == ILoadingMoreFooter.State.STATE_NO_MORE) {
            smoothScrollTo(getCriticalViewHeight())
            return false
        }
        if (mState == ILoadingMoreFooter.State.STATE_LOAD_ERROR) {
            smoothScrollTo(getCriticalViewHeight())
            return false
        }
        return if (mState == ILoadingMoreFooter.State.STATE_COMPLETE) {
            false
        } else false

    }

    /**
     * 用于从当前状态滑动都destHeight
     *
     * @param destHeight
     */
    protected fun smoothScrollTo(destHeight: Int) {
        val animator = ValueAnimator.ofInt(getVisibleHeight(), destHeight)
        animator.setDuration(300).start()
        animator.addUpdateListener { animation ->
            //最后地往回收view
            setVisibleHeight(animation.animatedValue as Int)
        }
        animator.start()
    }

    fun setVisibleHeight(height: Int) {
        var height = height
//        logd("aaaa", "上拉的高度:${height}")
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