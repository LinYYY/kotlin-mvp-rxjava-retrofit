package com.tech.mvpframework.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.ApplicationInfo.FLAG_SUPPORTS_RTL
import android.content.pm.PackageInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextPaint
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.tech.mvpframework.R
import com.tech.mvpframework.base.BaseApplication
import com.tech.mvpframework.constant.SPKey
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.*


/**
 *  create by Myking
 *  date : 2020/5/14 18:23
 *  description :
 */
class CommonUtils {

    companion object {
        @JvmStatic
        fun dp2px(context: Context, dpValue: Float): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dpValue,
                context.resources.displayMetrics
            ).toInt()
        }

        @JvmStatic
        fun sp2px(context: Context, spValue: Float): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                spValue,
                context.resources.displayMetrics
            ).toInt()
        }

        @JvmStatic
        fun getDisplayWidth(c: Context): Int {
            val wm: WindowManager = c.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            return wm.defaultDisplay.width
        }

        @JvmStatic
        fun getDisplayHeight(c: Context): Int {
            val wm: WindowManager = c.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            return wm.defaultDisplay.height
        }

        @JvmStatic
        fun getStatusBarHeight(context: Context): Int {
            val resourceId =
                context.resources.getIdentifier("status_bar_height", "dimen", "android")
            return context.resources.getDimensionPixelSize(resourceId)
        }

        /**
         * 测量字符高度
         *
         * @param text
         * @return
         */
        @JvmStatic
        fun getTextHeight(textPaint: TextPaint, text: String): Int {
            val bounds = Rect()
            textPaint.getTextBounds(text, 0, text.length, bounds)
            return bounds.height()
        }

        /**
         * 测量字符宽度
         *
         * @param textPaint
         * @param text
         * @return
         */
        @JvmStatic
        fun getTextWidth(textPaint: TextPaint, text: String?): Int {
            return textPaint.measureText(text).toInt()
        }

        /**
         * 解决InputMethodManager引起的内存泄漏
         * 在Activity的onDestroy方法里调用
         */
        @JvmStatic
        fun fixInputMethodManagerLeak(context: Context) {

            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val arr = arrayOf("mCurRootView", "mServedView", "mNextServedView")
            var field: Field? = null
            var objGet: Any? = null
            for (i in arr.indices) {
                val param = arr[i]
                try {
                    field = imm.javaClass.getDeclaredField(param)
                    if (!field.isAccessible) {
                        field.isAccessible = true
                    }
                    objGet = field.get(imm)
                    if (objGet != null && objGet is View) {
                        val view = objGet
                        if (view.context === context) {
                            // 被InputMethodManager持有引用的context是想要目标销毁的
                            field.set(imm, null) // 置空，破坏掉path to gc节点
                        } else {
                            // 不是想要目标销毁的，即为又进了另一层界面了，不要处理，避免影响原逻辑,也就不用继续for循环了
                            break
                        }
                    }
                } catch (t: Throwable) {
                    t.printStackTrace()
                }

            }

        }

        /**
         * 获取当前进程名
         */
        @JvmStatic
        fun getProcessName(pid: Int): String {
            var reader: BufferedReader? = null
            try {
                reader = BufferedReader(FileReader("/proc/$pid/cmdline"))
                var processName = reader.readLine()
                if (!TextUtils.isEmpty(processName)) {
                    processName = processName.trim { it <= ' ' }
                }
                return processName
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            } finally {
                try {
                    reader?.close()
                } catch (exception: IOException) {
                    exception.printStackTrace()
                }
            }
            return ""
        }

        /**
         * 格式化当前日期
         */
        @JvmStatic
        fun formatCurrentDate(): String {
            val format: SimpleDateFormat =
                if (Locale.getDefault().language == Locale.CHINA.language) {
                    SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                } else {
                    SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                }
            return format.format(Date())
        }

        /**
         * 格式化指定日期
         */
        @JvmStatic
        fun formatDate(date: Date): String {
            val format: SimpleDateFormat =
                if (Locale.getDefault().language == Locale.CHINA.language) {
                    SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                } else {
                    SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                }
            return format.format(date)
        }

        /**
         * 格式化指定日期
         */
        @JvmStatic
        fun formatTime(date: Date): String {
            val format: SimpleDateFormat =
                if (Locale.getDefault().language == Locale.CHINA.language) {
                    SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
                } else {
                    SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault())
                }
            return format.format(date)
        }

        @JvmStatic
        fun getPrintTime(): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
            return formatter.format(Date(System.currentTimeMillis()))
        }

        @JvmStatic
        fun getPkgName(context: Context): String {
            return context.packageName
        }

        @JvmStatic
        fun getAndroidID(context: Context): String {
            return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }

        @JvmStatic
        fun getOSVer(): String {
            return Build.VERSION.RELEASE
        }

        @JvmStatic
        fun getRom(): String {
            return Build.MANUFACTURER
        }

        @JvmStatic
        fun getPhoneModel(): String {
            return Build.MODEL
        }

        @JvmStatic
        fun isPhone(context: Context): Boolean {
            return !isPad(context)
        }

        @JvmStatic
        fun isPad(context: Context): Boolean {
            return context.resources.configuration.screenLayout.and(Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
        }

        @JvmStatic
        fun getCountryCode(context: Context): String {
            if (activeNetInfo == null) {
                val manager =
                    context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                activeNetInfo = manager.simCountryIso.toUpperCase()
            }
            return activeNetInfo!!
        }

        private var activeNetInfo: String? = null


        @JvmStatic
        fun getRealCountry(context: Context): String {
            var simCountry = ""
            if (hasSimCard(context)) {
                simCountry = getCountryCode(context)
            }
            return when {
                simCountry.isNotEmpty() -> simCountry
                else -> getLocaleCountry(context)
            }
        }

        @JvmStatic
        fun getLocaleCountry(context: Context): String {
            return context.resources.configuration.locale.country.toUpperCase()
        }

        @JvmStatic
        fun getLanCode(context: Context): String {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) context.resources.configuration.locales.get(
                0
            ).language.toLowerCase()
            else context.resources.configuration.locale.language.toLowerCase()
        }

        @JvmStatic
        fun getVerCode(context: Context): Int {
            return context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        }

        @JvmStatic
        fun getVerName(context: Context): String {
            if (verName == null) {
                verName = context.packageManager.getPackageInfo(context.packageName, 0).versionName
            }
            return verName!!
        }

        private var verName: String? = null

        fun getUid(context: Context): Int {
            return context.applicationInfo.uid
        }

        /**
         * 判断是否包含SIM卡
         *
         * @return 状态
         */
        fun hasSimCard(context: Context): Boolean {
            val telMgr =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val simState = telMgr.simState
            var result = true
            when (simState) {
                TelephonyManager.SIM_STATE_ABSENT -> result = false // 没有SIM卡
                TelephonyManager.SIM_STATE_UNKNOWN -> result = false
            }
            return result
        }

        /**
         * 分享一段文字出去
         *
         * @param text 分享的内容
         */
        @JvmStatic
        fun shareText(context: Context?, text: String?) {
            if (context == null || text == null) return
            val textIntent =
                Intent(Intent.ACTION_SEND)
            textIntent.type = "text/plain"
            textIntent.putExtra(Intent.EXTRA_TEXT, text)
            context.startActivity(
                Intent.createChooser(
                    textIntent,
                    context.resources.getString(R.string.app_name)
                )
            )
        }

        /**
         * 获取内存信息
         */
        @JvmStatic
        fun getMemoryInfo(context: Context): ActivityManager.MemoryInfo {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val info = ActivityManager.MemoryInfo()
            am.getMemoryInfo(info)
            return info
        }

        /**
         * 获取随机字符串
         */
        @JvmStatic
        fun getRadomString(targetStringLength: Int = 10): String {
            val leftLimit = 97 // letter 'a'

            val rightLimit = 122 // letter 'z'

            val random = Random()
            val buffer = StringBuilder(targetStringLength)
            for (i in 0 until targetStringLength) {
                val randomLimitedInt =
                    leftLimit + (random.nextFloat() * (rightLimit - leftLimit + 1)).toInt()
                buffer.append(randomLimitedInt.toChar())
            }
            return buffer.toString()

        }


        /**
         * 判断是否有虚拟按键
         */
        fun isNavigationBarExist(
            activity: Activity?,
            onNavigationStateListener: OnNavigationStateListener?
        ) {
            if (activity == null) {
                return
            }
            val height = getNavigationHeight(activity)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                activity.window.decorView
                    .setOnApplyWindowInsetsListener { v, windowInsets ->
                        var isShowing = false
                        var bottom = 0
                        if (windowInsets != null) {
                            bottom = windowInsets.systemWindowInsetBottom
                            isShowing = bottom == height
                        }
                        if (onNavigationStateListener != null && bottom <= height) {
                            onNavigationStateListener.onNavigationState(isShowing, bottom)
                        }
                        windowInsets
                    }
            }
        }

        /**
         * 获取虚拟按键高度
         */
        fun getNavigationHeight(activity: Context?): Int {
            if (activity == null) {
                return 0
            }
            val resources: Resources = activity.resources
            val resourceId: Int = resources.getIdentifier(
                "navigation_bar_height",
                "dimen", "android"
            )
            var height = 0
            if (resourceId > 0) {
                //获取NavigationBar的高度
                height = resources.getDimensionPixelSize(resourceId)
            }
            return height
        }

        fun getGmtTimeZone(): String {
            val t = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / (3600 * 1000)
            return "GMT+$t"
        }

        fun copyToClipboard(context: Context, data: String) {
            val cm: ClipboardManager =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val mClipData: ClipData =
                ClipData.newPlainText(context.resources.getString(R.string.app_name), data)
            cm.setPrimaryClip(mClipData)
        }

        /**
         * 判断是否进行阿拉伯镜像，判断取决于两个条件：<br></br>
         * 一丶Androidmanifest.xml中的supportsRtl<br></br>
         * 二、当前系统为阿拉伯语
         *
         * @return 是否为rtl
         */
        fun isRTL(): Boolean {
            val applicationInfo: ApplicationInfo =
                BaseApplication.instance.applicationInfo
            val hasRtlSupport =
                FLAG_SUPPORTS_RTL == applicationInfo.flags and FLAG_SUPPORTS_RTL
            val isRtl =
                TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL
            return hasRtlSupport && isRtl
        }

        /**
         * 是否安装某app
         * @param pkgName 包名
         */
        fun isInstalled(context: Context, pkgName: String): Boolean {
            val manager = context.packageManager
            val pkgList: List<PackageInfo> = manager.getInstalledPackages(0)
            for (pi in pkgList) {
                if (TextUtils.equals(pkgName, pi.packageName)) {
                    return true
                }
            }
            return false
        }
    }

    interface OnNavigationStateListener {
        fun onNavigationState(isShowing: Boolean, bottom: Int)
    }
}

