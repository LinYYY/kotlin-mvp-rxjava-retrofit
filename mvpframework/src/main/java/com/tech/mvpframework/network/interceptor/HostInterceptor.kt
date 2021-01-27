package com.tech.mvpframework.network.interceptor

import com.tech.mvpframework.utils.logd
import okhttp3.Interceptor
import okhttp3.Response

/**
 *  create by Myking
 *  date : 2020/5/29 10:40
 *  description :动态修改域名
 */
class HostInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var path = request.url.encodedPath

        //todo 根据需求设置域名
        val host = "test.com/"

        if (path.endsWith("/")) {
            path = path.substring(0, path.length - 1)
        }
        logd("HOST --> $host")

        val scheme = if (host == "test.com/") "https" else "http"
        val url = request.url.newBuilder().scheme(scheme).host(host).encodedPath(path).build()
        logd(url.toString())
        return chain.proceed(request.newBuilder().url(url).build())
    }
}