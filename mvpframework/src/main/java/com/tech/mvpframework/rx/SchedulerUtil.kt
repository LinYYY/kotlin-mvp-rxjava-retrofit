package com.tech.mvpframework.rx

import com.tech.mvpframework.rx.scheduler.ComputationMainScheduler
import com.tech.mvpframework.rx.scheduler.IoMainScheduler
import com.tech.mvpframework.rx.scheduler.NewThreadMainScheduler
import com.tech.mvpframework.rx.scheduler.TrampolineMainScheduler

/**
 *  create by Myking
 *  date : 2020/5/14 16:31
 *  description :
 */
object SchedulerUtil {

    fun <T> ioMainScheduler(): IoMainScheduler<T> {
        return IoMainScheduler()
    }

    fun <T> computationMainScheduler(): ComputationMainScheduler<T> {
        return ComputationMainScheduler()
    }

    fun <T> newTreadMainScheduler(): NewThreadMainScheduler<T> {
        return NewThreadMainScheduler()
    }

    fun <T> trampolineMainScheduler(): TrampolineMainScheduler<T> {
        return TrampolineMainScheduler()
    }

}