package com.tech.mvpframework.view.alert

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import com.tech.mvpframework.R
import kotlinx.android.synthetic.main.alert_layout.view.*

/**
 *  create by Myking
 *  date : 2020/8/12 17:19
 *  description :
 */
class Alert @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle), View.OnClickListener, Animation.AnimationListener,
    SwipeDismissTouchListener.DismissCallbacks {


    private var onShowListener: OnShowAlertListener? = null
    private var onHideListener: OnHideAlertListener? = null

    var enterAnimation: Animation = AnimationUtils.loadAnimation(context, R.anim.alert_slide_in)
    var exitAnimation: Animation = AnimationUtils.loadAnimation(context, R.anim.alert_slide_out)

    var duration = DISPLAY_TIME_IN_SECONDS

    private var enableInfiniteDuration: Boolean = false

    private var runningAnimation: Runnable? = null

    private var isDismissible = true


    private var vibrationEnabled = true
    private var soundUri: Uri? = null


    init {
        inflate(context, R.layout.alert_layout, this)
        isHapticFeedbackEnabled = true
        ViewCompat.setTranslationZ(this, Integer.MAX_VALUE.toFloat())
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        enterAnimation.setAnimationListener(this)

        animation = enterAnimation

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        enterAnimation.setAnimationListener(null)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.performClick()
        return super.onTouchEvent(event)
    }

    override fun onClick(v: View) {
        if (isDismissible) {
            hide()
        }
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        for (i in 0 until childCount) {
            getChildAt(i).visibility = visibility
        }
    }

    override fun onAnimationStart(animation: Animation) {
        if (!isInEditMode) {
            visibility = View.VISIBLE

            if (vibrationEnabled) {
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            soundUri?.let {
                val r = RingtoneManager.getRingtone(context, soundUri)
                r.play()
            }
        }
    }

    override fun onAnimationEnd(animation: Animation) {
        onShowListener?.onShow()

        startHideAnimation()
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private fun startHideAnimation() {
        if (!enableInfiniteDuration) {
            runningAnimation = Runnable { hide() }
            postDelayed(runningAnimation, duration)
        }
    }

    override fun onAnimationRepeat(animation: Animation) {

    }

    private fun hide() {
        try {
            exitAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    removeFromParent()
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })

            startAnimation(exitAnimation)
        } catch (ex: Exception) {
            Log.e(javaClass.simpleName, Log.getStackTraceString(ex))
        }
    }

    internal fun removeFromParent() {
        clearAnimation()
        visibility = View.GONE

        postDelayed(object : Runnable {
            override fun run() {
                try {
                    if (parent != null) {
                        try {
                            (parent as ViewGroup).removeView(this@Alert)

                            onHideListener?.onHide()
                        } catch (ex: Exception) {
                            Log.e(javaClass.simpleName, "Cannot remove from parent layout")
                        }
                    }
                } catch (ex: Exception) {
                    Log.e(javaClass.simpleName, Log.getStackTraceString(ex))
                }
            }
        }, CLEAN_UP_DELAY_MILLIS.toLong())
    }

    fun setDismissible(dismissible: Boolean) {
        this.isDismissible = dismissible
    }

    fun isDismissible(): Boolean {
        return isDismissible
    }


    fun enableSwipeToDismiss() {
        ll_alert_bg.let {
            it.setOnTouchListener(
                SwipeDismissTouchListener(
                    it,
                    object : SwipeDismissTouchListener.DismissCallbacks {
                        override fun canDismiss(): Boolean {
                            return true
                        }

                        override fun onDismiss(view: View) {
                            removeFromParent()
                        }

                        override fun onTouch(view: View, touch: Boolean) {

                        }
                    })
            )
        }
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        ll_alert_bg.setOnClickListener(listener)
    }

    fun setOnShowListener(listener: OnShowAlertListener) {
        this.onShowListener = listener
    }

    fun setOnHideListener(listener: OnHideAlertListener?) {
        this.onHideListener = listener
    }


    fun setVibrationEnabled(vibrationEnabled: Boolean) {
        this.vibrationEnabled = vibrationEnabled
    }

    fun setText(msg: String) {
        tv_alert_title.text = msg
    }

    fun setText(@StringRes stringId: Int) {
        tv_alert_title.setText(stringId)
    }

    fun setSound(soundUri: Uri?) {
        this.soundUri = soundUri
    }

    override fun canDismiss(): Boolean {
        return isDismissible
    }

    override fun onDismiss(view: View) {
        fl_click_shield?.removeView(ll_alert_bg)
    }

    override fun onTouch(view: View, touch: Boolean) {
        if (touch) {
            removeCallbacks(runningAnimation)
        } else {
            startHideAnimation()
        }
    }

    companion object {

        private const val CLEAN_UP_DELAY_MILLIS = 100

        private const val DISPLAY_TIME_IN_SECONDS: Long = 3000

    }
}