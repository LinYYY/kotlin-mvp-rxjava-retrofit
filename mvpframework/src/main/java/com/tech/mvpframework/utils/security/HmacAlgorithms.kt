package com.tech.mvpframework.utils.security

/**
 *  create by Myking
 *  date : 2020/5/7 15:56
 *  description :
 */

enum class HmacAlgorithms(private val algorithm: String) {

    HMAC_SHA_256("HmacSHA256"),
    HMAC_SHA_1("HmacSHA1");

    override fun toString(): String {
        return algorithm
    }

}