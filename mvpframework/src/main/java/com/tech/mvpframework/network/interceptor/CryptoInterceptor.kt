package com.tech.mvpframework.network.interceptor

import android.util.Base64
import com.tech.mvpframework.constant.Constant
import com.tech.mvpframework.utils.logd
import com.tech.mvpframework.utils.loge
import com.tech.mvpframework.utils.security.DesUtil
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 *  create by Myking
 *  date : 2020/5/20 11:28
 *  description :数据加密
 */
class CryptoInterceptor : Interceptor {

    companion object {
        private val MEDIA_TYPE =
            "application/json; charset=UTF-8".toMediaTypeOrNull()
        private val UTF_8 = Charset.forName("UTF-8")
        private const val HEADER_CRYPTO = "x-crypto"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val desKey = Constant.DES_KEY
        if (request.method == "POST") {
            val requestBody = request.body
            val buffer = Buffer()
            requestBody?.writeTo(buffer)
            val contentType = requestBody?.contentType()
            val charset: Charset =
                contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
            val body = DesUtil.encrypt(
                buffer.readString(charset),
                Base64.decode(desKey, Base64.DEFAULT)
            )
            request = request.newBuilder().post(
                RequestBody.create(
                    MEDIA_TYPE,
                    body
                )
            ).build()
            logd("加密完成")
        }

        var response = chain.proceed(request)

        if (response.headers[HEADER_CRYPTO] != null) {
            response.body?.let {
                val decodeString =
                    DesUtil.decrypt(it.bytes(), Base64.decode(desKey, Base64.DEFAULT))
                loge("解密后数据:$decodeString")
                response = response.newBuilder()
                    .body(ResponseBody.create(MEDIA_TYPE, decodeString))
                    .build()
            }
            logd("解密完成")
        }

        return response
    }
}