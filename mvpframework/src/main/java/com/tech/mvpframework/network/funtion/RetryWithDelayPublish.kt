package com.tech.mvpframework.network.funtion

import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function
import org.reactivestreams.Publisher
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 *  create by Myking
 *  date : 2020/8/18 16:21
 *  description :
 */
class RetryWithDelayPublish : Function<Flowable<out Throwable>, Publisher<*>> {


    private var maxRetryCount = 3 // 可重试次数
    private var retryDelayMillis: Long = 3000 // 重试等待时间

    constructor() {}

    constructor(retryDelayMillis: Long) {
        this.retryDelayMillis = retryDelayMillis
    }

    constructor(maxRetryCount: Int, retryDelayMillis: Long) {
        this.maxRetryCount = maxRetryCount
        this.retryDelayMillis = retryDelayMillis
    }

    override fun apply(flowable: Flowable<out Throwable>): Publisher<*> {
        return flowable
            .zipWith(
                Flowable.range(1, maxRetryCount + 1),
                BiFunction<Throwable, Int, Wrapper> { t1, t2 -> Wrapper(t2, t1) })
            .flatMap { wrapper ->
                val t = wrapper.throwable
                if ((t is ConnectException
                            || t is SocketTimeoutException
                            || t is TimeoutException
                            || t is HttpException)
                    && wrapper.index < maxRetryCount + 1
                ) {
                    Flowable.timer(retryDelayMillis * wrapper.index, TimeUnit.MILLISECONDS)
                } else Flowable.error<Any>(wrapper.throwable)
            }
    }

    private inner class Wrapper(val index: Int, val throwable: Throwable)
}