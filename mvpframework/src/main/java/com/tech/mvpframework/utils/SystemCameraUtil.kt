package com.tech.mvpframework.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException


/**
 * 调用系统相机
 */
object SystemCameraUtil {

    fun openSystemCamera(c: Activity, rootPath: String, code: Int): String? {
        var result: String? = null
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(c.packageManager) != null) {
            val imageFile = createImageFile(rootPath)
            var imageUri: Uri? = null
            if (imageFile != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    imageUri =
                        FileProvider.getUriForFile(c, "link.android.file.provider", imageFile)
                } else {
                    imageUri = Uri.fromFile(imageFile)
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                LogPrint.d("xx", "打开系统相机，保存图片路劲${imageFile.path}")
                c.startActivityForResult(intent, code)
                result = imageFile.path
            }
        }
        return result
    }

    fun openSystemCameraVideo(c: Activity, rootPath: String, code: Int): String? {
        var result: String? = null
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
//        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, (1024L * 1024L * 25L))
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30)
        if (intent.resolveActivity(c.packageManager) != null) {
            val videoFile = createVideoFile(rootPath)
            var videoUri: Uri? = null
            if (videoFile != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    videoUri =
                        FileProvider.getUriForFile(c, "link.android.file.provider", videoFile)
                } else {
                    videoUri = Uri.fromFile(videoFile)
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
                LogPrint.d("xx", "打开系统相机，保存视频路劲${videoFile.path}")
                c.startActivityForResult(intent, code)
                result = videoFile.path
            }
        }
        return result
    }

    fun onActivityResult(
        activity: Activity, targetCode: Int, requestCode: Int, resultCode: Int,
        data: Intent?, listen: SystemCameraUtilListen
    ) {
        if (requestCode == targetCode) {
            if (resultCode == Activity.RESULT_OK) {
                listen.onSuccess()
            } else {
                listen.onFail()
            }
        }
    }

    private fun createImageFile(rootPath: String): File? {
        var imageFile: File? = null
        val name = "camera_${System.currentTimeMillis()}"
        try {
            imageFile = File.createTempFile(name, ".jpg", File(rootPath))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return imageFile
    }

    private fun createVideoFile(rootPath: String): File? {
        var videoFile: File? = null
        val name = "camera_${System.currentTimeMillis()}"
        try {
            videoFile = File.createTempFile(name, ".mp4", File(rootPath))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return videoFile
    }

    interface SystemCameraUtilListen {
        fun onSuccess()
        fun onFail()
    }
}