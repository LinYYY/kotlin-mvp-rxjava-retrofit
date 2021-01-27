package com.tech.mvpframework.rx.scheduler

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 *  create by Myking
 *  date : 2020/5/14 16:22
 *  description :新线程
 */
class NewThreadMainScheduler<T> : BaseScheduler<T>(Schedulers.newThread(), AndroidSchedulers.mainThread()) {
}