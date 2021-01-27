package com.tech.mvpframework.rx.scheduler

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 *  create by Myking
 *  date : 2020/5/14 16:28
 *  description :工作队列，FIFO
 */
class TrampolineMainScheduler<T> :
    BaseScheduler<T>(Schedulers.trampoline(), AndroidSchedulers.mainThread()) {
}