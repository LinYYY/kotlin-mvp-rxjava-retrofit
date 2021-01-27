package com.tech.mvpframework.view.xrecyclerview

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import com.tech.mvpframework.R

/**
 *
 */
class SwipeItem : FrameLayout {

    internal var swipeWidth: Int = 0   // 该值为正值，实应为负

    internal var eventX = 0f // 记录手指位置
    internal var translationX = 0f  // 当前抽屉的打开程度

    constructor(context: Context) : super(context) {}

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) : super(
        context,
        attrs,
        defStyleAttr
    ) {

        val array =
            context.theme.obtainStyledAttributes(attrs, R.styleable.SwipeItem, defStyleAttr, 0)

        swipeWidth = array.getDimensionPixelSize(R.styleable.SwipeItem_swipe_size, 0)
        array.recycle()
    }


    fun setSwipeWidth(width: Int) {
        this.swipeWidth = width
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // 当有抽屉打开时，不是当前item，拦截事件
        if (XRecyclerView.curSwipeItem != null && XRecyclerView.curSwipeItem !== this) {
            if (ev.action == MotionEvent.ACTION_UP || ev.action == MotionEvent.ACTION_MOVE) {
                //点击或者移动的时候收起
                XRecyclerView.curSwipeItem?.closeSwipe()
            }
            return ev.action != MotionEvent.ACTION_DOWN
        } else {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    eventX = ev.x
                }
                MotionEvent.ACTION_MOVE -> {
                    val v = eventX - ev.x
                    if (Math.abs(v) > 20) {
                        // 打开抽屉
                        intercept = true
                    }
                    eventX = ev.x
                }
            }
            return intercept
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                eventX = event.x
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (intercept) {
                    if (XRecyclerView.curSwipeItem == null || XRecyclerView.curSwipeItem === this) {
                        requestDisallowInterceptTouchEvent(true)
                        XRecyclerView.curSwipeItem = this
                        val temp = event.x - eventX
                        translationX += temp
                        if (translationX > 0) {
                            translationX = 0f
                        } else if (translationX < -swipeWidth) {
                            translationX = (-swipeWidth).toFloat()
                        }

                        getChildAt(0).scrollTo((-translationX).toInt(), 0)
                        eventX = event.x
                    }
                } else {
                    val v = eventX - event.x
                    if (Math.abs(v) > 20) {
                        // 打开抽屉
                        intercept = true
                    }
                    eventX = event.x
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (XRecyclerView.curSwipeItem === this) {
                    endSwipe()
                }
            }
        }

        return super.onTouchEvent(event)
    }

    /**
     * 松手时如果超过一半，自动展开，否则关上
     */
    private fun endSwipe() {
        if (translationX < -swipeWidth / 2) {
            val valueAnimator = ValueAnimator.ofFloat(translationX, -swipeWidth.toFloat())
            valueAnimator.duration = 300
            valueAnimator.interpolator = DecelerateInterpolator()
            valueAnimator.addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                getChildAt(0).scrollTo((-animatedValue).toInt(), 0)
                translationX = animatedValue
            }
            valueAnimator.start()
            intercept = false
        } else {
            val valueAnimator = ValueAnimator.ofFloat(translationX, 0f)
            valueAnimator.duration = 300
            valueAnimator.interpolator = DecelerateInterpolator()
            valueAnimator.addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                getChildAt(0).scrollTo((-animatedValue).toInt(), 0)
                translationX = animatedValue
            }
            valueAnimator.start()
            intercept = false
            XRecyclerView.curSwipeItem = null
        }
    }


    fun closeSwipe(animated: Boolean = true) {
        if (animated) {
            val valueAnimator = ValueAnimator.ofFloat(translationX, 0f)
            valueAnimator.duration = 300
            valueAnimator.interpolator = DecelerateInterpolator()
            valueAnimator.addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                getChildAt(0).scrollTo((-animatedValue).toInt(), 0)
                translationX = animatedValue
            }
            valueAnimator.start()
        } else {
            translationX = 0f
            getChildAt(0).scrollTo(0, 0)
        }
        intercept = false
        XRecyclerView.curSwipeItem = null
    }

    fun dp2px(dpValue: Float): Int {
        val scale = resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    companion object {
        internal var intercept = false // 有抽屉打开就拦截（有一个被拦截，所有都被拦截）
    }
}
