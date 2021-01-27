package com.tech.mvpframework.view.alert

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Build
import androidx.annotation.RequiresApi
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration

internal class SwipeDismissTouchListener(
    private val swipeView: View,
    private val callbacks: DismissCallbacks
) : View.OnTouchListener {

    private val slop: Int
    private val minFlingVelocity: Int
    private val animationTime: Long
    private var viewHeight = 1


    private var downX: Float = 0.toFloat()
    private var downY: Float = 0.toFloat()
    private var swiping: Boolean = false
    private var swipingSlop: Int = 0
    private var velocityTracker: VelocityTracker? = null
    private var translationX: Float = 0.toFloat()
    private var translationY: Float = 0.toFloat()

    init {
        val vc = ViewConfiguration.get(swipeView.context)
        slop = vc.scaledTouchSlop
        minFlingVelocity = vc.scaledMinimumFlingVelocity * 16
        animationTime = swipeView.context.resources.getInteger(
            android.R.integer.config_shortAnimTime
        ).toLong()
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR1)
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {

        motionEvent.offsetLocation(0f, translationY)

        if (viewHeight < 2) {
            viewHeight = this.swipeView.height
        }

        when (motionEvent.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = motionEvent.rawX
                downY = motionEvent.rawY
                if (callbacks.canDismiss()) {
                    velocityTracker = VelocityTracker.obtain()
                    velocityTracker!!.addMovement(motionEvent)
                }
                callbacks.onTouch(view, true)
                return false
            }
            MotionEvent.ACTION_UP -> {
                velocityTracker?.run {
                    val deltaY = motionEvent.rawY - downY
                    this.addMovement(motionEvent)
                    this.computeCurrentVelocity(1000)
                    val velocityX = this.xVelocity
                    val velocityY = this.yVelocity
                    val absVelocityX = Math.abs(velocityX)
                    val absVelocityY = Math.abs(this.yVelocity)
                    var dismiss = false
                    var dismissTop = false

                    if (Math.abs(deltaY) > viewHeight / 2 && swiping) {
                        dismiss = true
                        dismissTop = deltaY < 0
                    } else if (minFlingVelocity <= absVelocityY && absVelocityX < absVelocityY && swiping) {
                        dismiss = velocityY < 0 == deltaY < 0
                        dismissTop = this.yVelocity < 0
                    }
                    if (dismiss) {
                        swipeView.animate()
                            .translationY((if (dismissTop) viewHeight else -viewHeight).toFloat())
                            .alpha(0f)
                            .setDuration(animationTime)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    performDismiss()
                                }
                            })
                    } else if (swiping) {
                        swipeView.animate()
                            .translationY(0f)
                            .alpha(1f)
                            .setDuration(animationTime)
                            .setListener(null)
                        callbacks.onTouch(view, false)
                    }
                    this.recycle()
                    velocityTracker = null
                    translationX = 0f
                    translationY = 0f
                    downX = 0f
                    downY = 0f
                    swiping = false
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                velocityTracker?.run {
                    swipeView.animate()
                        .translationY(0f)
                        .alpha(1f)
                        .setDuration(animationTime)
                        .setListener(null)
                    this.recycle()
                    velocityTracker = null
                    translationX = 0f
                    translationY = 0f
                    downX = 0f
                    downY = 0f
                    swiping = false
                }
            }
            MotionEvent.ACTION_MOVE -> {
                velocityTracker?.run {
                    this.addMovement(motionEvent)
                    val deltaX = motionEvent.rawX - downX
                    val deltaY = motionEvent.rawY - downY
                    if (Math.abs(deltaY) > slop  && deltaY < 0) {
                        //只响应上划
                        swiping = true
                        swipingSlop = if (deltaY > 0) slop else -slop
                        swipeView.parent.requestDisallowInterceptTouchEvent(true)

                        val cancelEvent = MotionEvent.obtain(motionEvent)
                        cancelEvent.action =
                            MotionEvent.ACTION_CANCEL or (motionEvent.actionIndex shl MotionEvent.ACTION_POINTER_INDEX_SHIFT)
                        swipeView.onTouchEvent(cancelEvent)
                        cancelEvent.recycle()
                    }

                    if (swiping && deltaY < 0) {
                        translationY = deltaY
                        swipeView.translationY = deltaY - swipingSlop
                        swipeView.alpha = Math.max(
                            0f, Math.min(
                                1f,
                                1f - 2f * Math.abs(deltaY) / viewHeight
                            )
                        )
                        return true
                    }
                }
            }
            else -> {
                view.performClick()
                return false
            }
        }
        return false
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private fun performDismiss() {

        val lp = swipeView.layoutParams
        val originalHeight = swipeView.height

        val animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(animationTime)

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                callbacks.onDismiss(swipeView)
                swipeView.alpha = 1f
                swipeView.translationY = 0f
                lp.height = originalHeight
                swipeView.layoutParams = lp
            }
        })

        animator.addUpdateListener { valueAnimator ->
            lp.height = valueAnimator.animatedValue as Int
            swipeView.layoutParams = lp
        }

        animator.start()
    }

    internal interface DismissCallbacks {
        fun canDismiss(): Boolean

        fun onDismiss(view: View)

        fun onTouch(view: View, touch: Boolean)
    }
}