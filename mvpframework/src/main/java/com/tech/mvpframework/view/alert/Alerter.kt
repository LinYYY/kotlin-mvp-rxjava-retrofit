package com.tech.mvpframework.view.alert

import android.app.Activity
import android.media.RingtoneManager
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.annotation.AnimRes
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import com.tech.mvpframework.R
import java.lang.ref.WeakReference

/**
 *  create by Myking
 *  date : 2020/8/12 17:55
 *  description :
 */
class Alerter private constructor() {

    private var alert: Alert? = null

    private val activityDecorView: ViewGroup?
        get() {
            var decorView: ViewGroup? = null

            activityWeakReference?.get()?.let {
                decorView = it.window.decorView as ViewGroup
            }

            return decorView
        }

    companion object {

        private var activityWeakReference: WeakReference<Activity>? = null

        @JvmStatic
        fun create(activity: Activity?): Alerter {
            requireNotNull(activity) { "Activity cannot be null!" }

            val alerter = Alerter()

            clearCurrent(activity)

            alerter.setActivity(activity)
            alerter.alert = Alert(activity)

            return alerter
        }


        @JvmStatic
        fun clearCurrent(activity: Activity?) {
            (activity?.window?.decorView as? ViewGroup)?.let {
                for (i in 0..it.childCount) {
                    val childView =
                        if (it.getChildAt(i) is Alert) it.getChildAt(i) as Alert else null
                    if (childView != null && childView.windowToken != null) {
                        ViewCompat.animate(childView).alpha(0f)
                            .withEndAction(getRemoveViewRunnable(childView))
                    }
                }
            }
        }


        @JvmStatic
        fun hide() {
            activityWeakReference?.get()?.let {
                clearCurrent(it)
            }
        }

        @JvmStatic
        val isShowing: Boolean
            get() {
                var isShowing = false

                activityWeakReference?.get()?.let {
                    isShowing = it.findViewById<View>(R.id.ll_alert_bg) != null
                }

                return isShowing
            }

        private fun getRemoveViewRunnable(childView: Alert?): Runnable {
            return Runnable {
                childView?.let {
                    (childView.parent as? ViewGroup)?.removeView(childView)
                }
            }
        }
    }

    fun show(): Alert? {

        activityWeakReference?.get()?.let {
            it.runOnUiThread {
                activityDecorView?.addView(alert)
            }
        }

        return alert
    }


    fun setOnClickListener(onClickListener: View.OnClickListener): Alerter {
        alert?.setOnClickListener(onClickListener)
        return this
    }

    fun setDuration(milliseconds: Long): Alerter {
        alert?.duration = milliseconds
        return this
    }

    fun setOnShowListener(listener: OnShowAlertListener): Alerter {
        alert?.setOnShowListener(listener)
        return this
    }

    fun setOnHideListener(listener: OnHideAlertListener?): Alerter {
        alert?.setOnHideListener(listener)
        return this
    }

    fun enableSwipeToDismiss(): Alerter {
        alert?.enableSwipeToDismiss()
        return this
    }

    fun enableVibration(enable: Boolean): Alerter {
        alert?.setVibrationEnabled(enable)
        return this
    }

    @JvmOverloads
    fun setSound(uri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)): Alerter {
        alert?.setSound(uri)
        return this
    }

    fun setDismissable(dismissible: Boolean): Alerter {
        alert?.setDismissible(dismissible)
        return this
    }

    fun setEnterAnimation(@AnimRes animation: Int): Alerter {
        alert?.enterAnimation = AnimationUtils.loadAnimation(alert?.context, animation)
        return this
    }

    fun setExitAnimation(@AnimRes animation: Int): Alerter {
        alert?.exitAnimation = AnimationUtils.loadAnimation(alert?.context, animation)
        return this
    }

    fun setText(msg: String): Alerter {
        alert?.setText(msg)
        return this
    }

    fun setText(@StringRes stringId: Int): Alerter {
        alert?.setText(stringId)
        return this
    }

    private fun setActivity(activity: Activity) {
        activityWeakReference = WeakReference(activity)
    }

}