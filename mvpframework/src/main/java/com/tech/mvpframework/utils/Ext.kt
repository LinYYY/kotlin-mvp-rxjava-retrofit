package com.tech.mvpframework.utils

import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import java.text.SimpleDateFormat
import java.util.*

/**
 *  create by Myking
 *  date : 2020/5/14 19:17
 *  description :常用的扩展方法
 */
/** Log相关  **/
fun Any.loge(msg: String?) {
    LogPrint.e("meete", msg ?: "")
}

fun Any.loge(tag: String?, msg: String?) {
    LogPrint.e(tag ?: this.javaClass.simpleName, msg ?: "")
}

fun Any.logd(msg: String?) {
    LogPrint.d("meete", msg ?: "")
}

fun Any.logd(tag: String?, msg: String?) {
    LogPrint.d(tag ?: this.javaClass.simpleName, msg ?: "")
}
/** Log相关 end **/

/** TextView相关  **/
fun TextView.color(@ColorRes colorId: Int) {
    this.setTextColor(context.resources.getColor(colorId))
}

fun TextView.padding(
    @DimenRes left: Int,
    @DimenRes top: Int,
    @DimenRes right: Int,
    @DimenRes bottom: Int
) {
    this.setPadding(
        context.resources.getDimension(left).toInt(),
        context.resources.getDimension(top).toInt(),
        context.resources.getDimension(right).toInt(),
        context.resources.getDimension(bottom).toInt()
    )
}

fun TextView.backgroundId(@DrawableRes resId: Int) {
    this.background = this.context.resources.getDrawable(resId)
}

fun TextView.drawableLeft(@DrawableRes resId: Int) {
    val drawable = this.context.resources.getDrawable(resId)
    drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
    this.setCompoundDrawables(drawable, null, null, null)
}

fun TextView.drawableTop(@DrawableRes resId: Int) {
    val drawable = this.context.resources.getDrawable(resId)
    drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
    this.setCompoundDrawables(null, drawable, null, null)
}

fun TextView.drawableRight(@DrawableRes resId: Int) {
    val drawable = this.context.resources.getDrawable(resId)
    drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
    this.setCompoundDrawables(null, null, drawable, null)
}

fun TextView.drawableBottom(@DrawableRes resId: Int) {
    val drawable = this.context.resources.getDrawable(resId)
    drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
    this.setCompoundDrawables(null, null, null, drawable)
}
/** TextView相关 end  **/

/** ImageView相关  **/
fun ImageView.load(
    url: String,
    @DrawableRes placeholder: Int? = null,
    userCenterCrop: Boolean = true
) {
    ImageLoader.load(this.context, url, this, placeholder, userCenterCrop = userCenterCrop)
}

fun ImageView.loadCircle(url: String, @DrawableRes placeholder: Int? = null) {
    ImageLoader.loadCircleImage(this.context, url, this, placeholder)
}

fun ImageView.loadRound(
    url: String,
    radius: Int = 5,
    @DrawableRes placeholder: Int? = null,
    userCenterCrop: Boolean = true
) {
    ImageLoader.loadRoundImage(this.context, url, this, radius, placeholder, userCenterCrop)
}

/** ImageView相关 end **/

/** String相关  **/

/**
 * String 转 Calendar
 */
fun String.stringToCalendar(): Calendar {
    val format: SimpleDateFormat = if (Locale.getDefault().language == Locale.CHINA.language) {
        SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    } else {
        SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    }
    val date = format.parse(this)
    val calendar = Calendar.getInstance()
    calendar.time = date
    return calendar
}

/**
 * String 转 Date
 */
fun String.stringToDate(): Date {
    val format: SimpleDateFormat = if (Locale.getDefault().language == Locale.CHINA.language) {
        SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    } else {
        SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    }
    return format.parse(this)
}
/** String相关 end  **/