package com.tech.mvpframework.download


interface DownloadListener {
    fun onDownloadError(errorMsg: String?)
    fun onDownloadFinish(path: String)
}