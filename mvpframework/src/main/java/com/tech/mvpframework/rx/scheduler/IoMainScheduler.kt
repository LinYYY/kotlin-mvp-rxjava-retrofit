package com.tech.mvpframework.rx.scheduler

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 *  create by Myking
 *  date : 2020/5/14 16:18
 *  description :适合IO密集型
 */
class IoMainScheduler<T> : BaseScheduler<T>(Schedulers.io(), AndroidSchedulers.mainThread()) {
}