package com.tech.mvpframework.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 *  create by Myking
 *  date : 2020/5/28 10:30
 *  description :添加公共 query参数
 */
class QueryStringInterceptor : Interceptor {

    companion object {
        private const val TIMESTAMP = "timestamp"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url =
            request.url.newBuilder()
                .addQueryParameter(TIMESTAMP, System.currentTimeMillis().toString())
                .build()

        return chain.proceed(request.newBuilder().url(url).build())
    }
}