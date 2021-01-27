package com.tech.mvpframework.utils.security

import java.nio.charset.Charset
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 *  create by Myking
 *  date : 2020/5/7 16:00
 *  description :
 */
object HmacUtil {
    fun hmacSha256(key: ByteArray?, valueToDigest: ByteArray?): ByteArray {
        return try {
            getHmacSha256(key).doFinal(valueToDigest)
        } catch (e: IllegalStateException) {
            // cannot happen
            throw IllegalArgumentException(e)
        }
    }
    fun hmacSha1(key: ByteArray?, valueToDigest: ByteArray?): ByteArray {
        return try {
            getHmacSha1(key).doFinal(valueToDigest)
        } catch (e: IllegalStateException) {
            // cannot happen
            throw IllegalArgumentException(e)
        }
    }

    fun getHmacSha256(key: ByteArray?): Mac {
        return getInitializedMac(
            HmacAlgorithms.HMAC_SHA_256,
            key
        )
    }

    fun getHmacSha1(key: ByteArray?): Mac {
        return getInitializedMac(
            HmacAlgorithms.HMAC_SHA_1,
            key
        )
    }

    fun getInitializedMac(
        algorithm: HmacAlgorithms,
        key: ByteArray?
    ): Mac {
        return getInitializedMac(algorithm.toString(), key)
    }

    fun getInitializedMac(algorithm: String?, key: ByteArray?): Mac {
        requireNotNull(key) { "Null key" }
        return try {
            val keySpec = SecretKeySpec(key, algorithm)
            val mac = Mac.getInstance(algorithm)
            mac.init(keySpec)
            mac
        } catch (e: NoSuchAlgorithmException) {
            throw IllegalArgumentException(e)
        } catch (e: InvalidKeyException) {
            throw IllegalArgumentException(e)
        }
    }

    fun hmacSha256(key: String?, valueToDigest: String?): ByteArray {
        return hmacSha256(getBytesUtf8(key), getBytesUtf8(valueToDigest))
    }

    fun hmacSha1(key: String?, valueToDigest: String?): ByteArray {
        return hmacSha1(getBytesUtf8(key), getBytesUtf8(valueToDigest))
    }

    private fun getBytes(
        string: String?,
        charset: Charset
    ): ByteArray? {
        return string?.toByteArray(charset)
    }

    fun getBytesUtf8(string: String?): ByteArray? {
        return getBytes(string, Charset.forName("UTF-8"))
    }
}