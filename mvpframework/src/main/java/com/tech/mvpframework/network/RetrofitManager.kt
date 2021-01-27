package com.tech.mvpframework.network

import com.tech.mvpframework.constant.Constant.BASE_URL
import com.tech.mvpframework.network.interceptor.*
import com.tech.mvpframework.utils.logd
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 *  create by Myking
 *  date : 2020/5/14 17:04
 *  description :
 */
object RetrofitManager {

    private const val DEFAULT_TIMEOUT: Long = 15
    private var retrofit: Retrofit? = null

    private val servicesMap: HashMap<String, Any> = HashMap()

    private fun getRetrofit(): Retrofit? {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        }
        return retrofit
    }

    /**
     * 获取 OkHttpClient
     */
    private fun getOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient().newBuilder()
        val httpLoggingInterceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                logd("OkHttp", message)
            }
        })
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY


        builder.run {
            addInterceptor(HostInterceptor())//动态替换域名
            addInterceptor(QueryStringInterceptor())//添加公共Query
            addInterceptor(HeaderInterceptor())//添加公共header
            addInterceptor(httpLoggingInterceptor)//Log拦截器
            addInterceptor(CryptoInterceptor())//body加密
            addInterceptor(CacheInterceptor())//设置缓存
            connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            retryOnConnectionFailure(true) // 错误重连
        }
        return builder.build()
    }

    @JvmStatic
    fun <T> getService(clazz: Class<T>): T {
        var service = servicesMap[clazz.simpleName]
        if (service == null) {
            service = getRetrofit()?.create(clazz)
            servicesMap[clazz.simpleName] = service!!
        }
        return service as T
    }
}