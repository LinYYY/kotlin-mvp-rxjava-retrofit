package com.tech.mvpframework.download

import android.content.Context
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class Downloader(
    private val context: Context,
    private val downloadUrl: String,
    private var listener: DownloadListener?
) : IDownload {

    private var client: OkHttpClient = OkHttpClient()
    private var call: Call? = null

    override fun download() {
        val request = Request.Builder()
            .url(downloadUrl)
            .build()
        call = client.newCall(request)

        val buffer = ByteArray(1024 * 1024)
        var len: Int
        var input: InputStream? = null
        var output: FileOutputStream? = null
        try {
            val response = call!!.execute()
            input = response.body!!.byteStream()
            val file = File(context.externalCacheDir, System.currentTimeMillis().toString())
            output = FileOutputStream(file)
            while (input.read(buffer).apply { len = this } != -1) {
                output.write(buffer, 0, len)
            }
            output.flush()
            listener?.onDownloadFinish(file.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            listener?.onDownloadError(e.message)
        } finally {
            try {
                input?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                output?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun cancel() {
        call?.cancel()
    }

    override fun destroy() {
        cancel()
        call = null
        listener = null
    }
}