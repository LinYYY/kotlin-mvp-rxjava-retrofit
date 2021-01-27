package com.tech.mvpframework.network.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.telephony.TelephonyManager
import java.io.IOException
import java.net.HttpURLConnection
import java.net.NetworkInterface
import java.net.SocketException
import java.net.URL

/**
 *  create by Myking
 *  date : 2020/5/14 17:15
 *  description :
 */
class NetWorkUtil {

    companion object {

        const val NET_CNNT_OK = 1 // NetworkAvailable
        const val NET_CNNT_TIMEOUT = 2 // no NetworkAvailable
        const val NET_NOT_PREPARE = 3 // Net no ready
        const val NET_ERROR = 4 //net error

        const val NET_TYPE_UNKNOWN = "unknown"
        const val NET_TYPE_WIFI = "wifi"
        const val NET_TYPE_2G = "gprs"
        const val NET_TYPE_3G = "3g"
        const val NET_TYPE_4G = "4g"

        private const val TIMEOUT = 3000 // TIMEOUT

        /**
         * check NetworkAvailable
         *
         * @param context
         * @return
         */
        @JvmStatic
        fun isNetworkAvailable(context: Context): Boolean {
            val manager = context.applicationContext.getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager
            val info = manager.activeNetworkInfo
            return !(null == info || !info.isAvailable)
        }

        /**
         * check NetworkConnected
         *
         * @param context
         * @return
         */
        @JvmStatic
        fun isNetworkConnected(context: Context): Boolean {
            val manager =
                context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val info = manager.activeNetworkInfo
            return !(null == info || !info.isConnected)
        }

        /**
         * 得到ip地址
         *
         * @return
         */
        @JvmStatic
        fun getLocalIpAddress(): String {
            var ret = ""
            try {
                val en = NetworkInterface.getNetworkInterfaces()
                while (en.hasMoreElements()) {
                    val enumIpAddress = en.nextElement().inetAddresses
                    while (enumIpAddress.hasMoreElements()) {
                        val netAddress = enumIpAddress.nextElement()
                        if (!netAddress.isLoopbackAddress) {
                            ret = netAddress.hostAddress.toString()
                        }
                    }
                }
            } catch (ex: SocketException) {
                ex.printStackTrace()
            }

            return ret
        }


        /**
         * ping "https://www.github.com"
         *
         * @return
         */
        @JvmStatic
        private fun pingNetWork(): Boolean {
            var result = false
            var httpUrl: HttpURLConnection? = null
            try {
                httpUrl = URL("https://www.github.com")
                    .openConnection() as HttpURLConnection
                httpUrl.connectTimeout = TIMEOUT
                httpUrl.connect()
                result = true
            } catch (e: IOException) {
            } finally {
                httpUrl?.disconnect()
            }
            return result
        }

        /**
         * check is3G
         *
         * @param context
         * @return boolean
         */
        @JvmStatic
        fun is3G(context: Context): Boolean {
            val connectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetInfo = connectivityManager.activeNetworkInfo
            return activeNetInfo != null && (activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_UMTS
                    || activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_EVDO_0
                    || activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_EVDO_A
                    || activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_HSDPA
                    || activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_HSUPA
                    || activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_HSPA
                    || activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_EVDO_B
                    || activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_EHRPD
                    || activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_HSPAP
                    || activeNetInfo.subtype == 17//TelephonyManager.NETWORK_TYPE_TD_SCDMA
                    )
        }

        fun is3G(activeNetInfo: NetworkInfo?): Boolean {
            return activeNetInfo != null && (activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_UMTS
                    || activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_EVDO_0
                    || activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_EVDO_A
                    || activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_HSDPA
                    || activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_HSUPA
                    || activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_HSPA
                    || activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_EVDO_B
                    || activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_EHRPD
                    || activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_HSPAP
                    || activeNetInfo.subtype == 17//TelephonyManager.NETWORK_TYPE_TD_SCDMA
                    )
        }

        /**
         * check is4G
         *
         * @param context
         * @return boolean
         */
        @JvmStatic
        fun is4G(context: Context): Boolean {
            val connectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetInfo = connectivityManager.activeNetworkInfo
            return activeNetInfo != null && (activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_LTE
                    || activeNetInfo.subtype == 18//TelephonyManager.NETWORK_TYPE_IWLAN
                    || activeNetInfo.subtype == 19 //TelephonyManager.NETWORK_TYPE_LTE_CA
                    )
        }

        fun is4G(activeNetInfo: NetworkInfo?): Boolean {
            return activeNetInfo != null && (activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_LTE
                    || activeNetInfo.subtype == 18//TelephonyManager.NETWORK_TYPE_IWLAN
                    || activeNetInfo.subtype == 19 //TelephonyManager.NETWORK_TYPE_LTE_CA
                    )
        }

        /**
         * isWifi
         *
         * @param context
         * @return boolean
         */
        @JvmStatic
        fun isWifi(context: Context): Boolean {
            val connectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetInfo = connectivityManager.activeNetworkInfo
            return activeNetInfo != null && activeNetInfo.type == ConnectivityManager.TYPE_WIFI
        }

        fun isWifi(activeNetInfo: NetworkInfo?): Boolean {
            return activeNetInfo != null && activeNetInfo.type == ConnectivityManager.TYPE_WIFI
        }

        /**
         * is2G
         *
         * @param context
         * @return boolean
         */
        @JvmStatic
        fun is2G(context: Context): Boolean {
            val connectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetInfo = connectivityManager.activeNetworkInfo
            return activeNetInfo != null && (activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_EDGE
                    || activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_GPRS || activeNetInfo
                .subtype == TelephonyManager.NETWORK_TYPE_CDMA)
        }

        fun is2G(activeNetInfo: NetworkInfo?): Boolean {
            return activeNetInfo != null && (activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_EDGE
                    || activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_GPRS || activeNetInfo
                .subtype == TelephonyManager.NETWORK_TYPE_CDMA)
        }

        /**
         * 检测是否使用vpn
         */
        fun isVpn(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as (ConnectivityManager)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cm.getNetworkInfo(ConnectivityManager.TYPE_VPN)?.isConnectedOrConnecting ?: false
            } else {
                return false
            }
        }

        /**
         * is wifi on
         */
        @JvmStatic
        fun isWifiEnabled(context: Context): Boolean {
            val mgrConn = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val mgrTel = context
                .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return mgrConn.activeNetworkInfo != null && mgrConn
                .activeNetworkInfo.state == NetworkInfo.State.CONNECTED || mgrTel
                .networkType == TelephonyManager.NETWORK_TYPE_UMTS
        }

        /**
         * 判断MOBILE网络是否可用
         */
        @JvmStatic
        fun isMobile(context: Context?): Boolean {
            if (context != null) {
                //获取手机所有连接管理对象(包括对wi-fi,net等连接的管理)
                val manager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                //获取NetworkInfo对象
                val networkInfo = manager.activeNetworkInfo
                //判断NetworkInfo对象是否为空 并且类型是否为MOBILE
                if (null != networkInfo && networkInfo.type == ConnectivityManager.TYPE_MOBILE)
                    return networkInfo.isAvailable
            }
            return false
        }

        /**
         * 获取网络类型
         */
        @JvmStatic
        fun getNetWorkType(context: Context): String {
            val connectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            if (isWifi(activeNetInfo)) return NET_TYPE_WIFI
            if (is4G(activeNetInfo)) return NET_TYPE_4G
            if (is3G(activeNetInfo)) return NET_TYPE_3G
            if (is2G(activeNetInfo)) return NET_TYPE_2G
            return NET_TYPE_UNKNOWN
        }
    }
}