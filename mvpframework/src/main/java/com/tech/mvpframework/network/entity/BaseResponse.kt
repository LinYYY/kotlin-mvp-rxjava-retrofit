package com.tech.mvpframework.network.entity

import com.google.gson.annotations.SerializedName

/**
 *  create by Myking
 *  date : 2020/5/14 16:36
 *  description :
 */
class BaseResponse<T> {

    @SerializedName("error_code")
    var errorCode: Int = 0

    @SerializedName("error_message")
    var errorMsg: String = "unknown error"

    var data: T? = null
}