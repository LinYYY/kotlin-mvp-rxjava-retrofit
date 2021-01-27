package com.tech.mvpframework.download

interface IDownload {
    fun download()
    fun cancel()
    fun destroy()
}