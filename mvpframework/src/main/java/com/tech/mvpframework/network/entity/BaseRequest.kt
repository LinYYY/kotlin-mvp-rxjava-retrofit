package com.tech.mvpframework.network.entity

import com.tech.mvpframework.base.BaseApplication
import com.tech.mvpframework.network.utils.NetWorkUtil
import com.tech.mvpframework.utils.CommonUtils
import java.io.Serializable

/**
 *  create by Myking
 *  date : 2020/5/20 13:49
 *  description :
 */
open class BaseRequest : Serializable {
    var device: Device = Device()

    data class Device(
        val device_id: String = CommonUtils.getAndroidID(BaseApplication.context)
    ) : Serializable
}