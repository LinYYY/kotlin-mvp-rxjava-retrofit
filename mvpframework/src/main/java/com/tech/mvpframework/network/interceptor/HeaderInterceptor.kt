package com.tech.mvpframework.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 *  create by Myking
 *  date : 2020/5/20 10:58
 *  description :用于添加公共头部
 */
class HeaderInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        //todo 添加公共头部
        val newRequest = request.newBuilder()
            .addHeader("header", "公共头部")
            .build()

        return chain.proceed(newRequest)
    }

}