package com.tech.mvpframework.rx.scheduler

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 *  create by Myking
 *  date : 2020/5/14 16:26
 *  description :适合计算密集型
 */
class ComputationMainScheduler<T> : BaseScheduler<T>(Schedulers.computation(), AndroidSchedulers.mainThread()) {
}