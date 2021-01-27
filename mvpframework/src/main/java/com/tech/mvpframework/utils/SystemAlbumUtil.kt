package com.tech.mvpframework.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.net.toFile
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

/**
 * 打开系统相册
 */
object SystemAlbumUtil {
    const val TYPE_PHOTO = 0
    const val TYPE_VIDEO = 1
    const val TYPE_PHOTO_AND_VIDEO = 2

    /**
     * @param code 和[SystemAlbumUtil.onActivityResult]的targetCode对应
     * @param mineType 0：照片，1：视频，2：视频和图片
     */
    fun openSystemAlbum(activity: Activity, code: Int, mineType: Int = TYPE_PHOTO) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        val mimeTypes = when (mineType) {
            TYPE_PHOTO -> arrayOf("image/jpeg", "image/png")
            TYPE_VIDEO -> arrayOf("video/mp4")
            TYPE_PHOTO_AND_VIDEO -> arrayOf("video/mp4", "image/jpeg", "image/png")
            else -> arrayOf("image/jpeg", "image/png")
        }
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        activity.startActivityForResult(intent, code)
    }

    /**
     * [SystemAlbumUtil.openSystemAlbum]配套使用
     */
    fun onActivityResult(
        activity: Activity, targetCode: Int, requestCode: Int, resultCode: Int,
        data: Intent?, callBack: SystemAlbumCallBack
    ) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == targetCode && data != null) {
                val uri = data.data
                if (uri != null) {
                    val path = getRealPathFromUri(activity, uri)
                    if (path != null) {
                        callBack.onResult(path)
                        return
                    }
                }
            }
        }
        callBack.onResult(null)
    }

    interface SystemAlbumCallBack {
        fun onResult(uri: String?)
    }

    /**
     * 获取图片的content uri
     *
     * @param context
     * @param filePath 图片路径
     * @return
     */
    fun getImageContentUri(context: Context, filePath: String): Uri? {
        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID), MediaStore.Images.Media.DATA + "=? ",
            arrayOf(filePath), null
        )
        var uri: Uri? = null

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
                val baseUri = Uri.parse("content://media/external/images/media")
                uri = Uri.withAppendedPath(baseUri, "" + id)
            }

            cursor.close()
        }

        if (uri == null) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DATA, filePath)
            uri =
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        }

        return uri
    }

    /**
     * 根据Uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    fun getRealPathFromUri(context: Context, uri: Uri): String? {
        val sdkVersion = Build.VERSION.SDK_INT
        return when {
            sdkVersion >= 19 -> { // api >= 19
                getRealPathFromUriAboveApi19(context, uri)
            }
            else -> { // api < 19
                getRealPathFromUriBelowAPI19(context, uri)
            }
        }
    }

    @SuppressLint("NewApi")
    private fun uriToFileQ(context: Context, uri: Uri): File? =
        if (uri.scheme == ContentResolver.SCHEME_FILE)
            uri.toFile()
        else if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            //把文件保存到沙盒
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.let { cursors ->
                if (cursors.moveToFirst()) {
                    try {
                        val ois = context.contentResolver.openInputStream(uri)
                        val displayName =
                            cursors.getString(cursors.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                        ois?.let {
                            File(
                                context.externalCacheDir!!.absolutePath,
                                "${Random.nextInt(0, 9999)}$displayName"
                            ).apply {
                                val fos = FileOutputStream(this)
                                ois.copyTo(fos)
                                ois.close()
                                fos.close()
                                cursors.close()
                            }
                        }
                    } catch (e: Exception) {
                        cursors.close()
                        return null
                    }

                } else {
                    cursors.close()
                    null
                }
            }

        } else null


    /**
     * 适配api19以下(不包括api19),根据uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */

    private fun getRealPathFromUriBelowAPI19(context: Context, uri: Uri): String? {
        return getDataColumn(context, uri, null, null)
    }


    /**
     * 适配api19及以上,根据uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */

    @SuppressLint("NewApi")
    private fun getRealPathFromUriAboveApi19(context: Context, uri: Uri): String? {
        var filePath = uri.path
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // 如果是document类型的 uri, 则通过document id来进行处理
            val documentId = DocumentsContract.getDocumentId(uri)
            if (isMediaDocument(uri)) { // MediaProvider
                // 使用':'分割
                val id =
                    documentId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                val selection = MediaStore.Images.Media._ID + "=?"
                val selectionArgs = arrayOf(id)
                filePath = getDataColumn(
                    context,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    selection,
                    selectionArgs
                )
            } else if (isDownloadsDocument(uri)) { // DownloadsProvider
                filePath = when {
                    documentId.startsWith("raw:") -> {
                        documentId.replaceFirst("raw:", "")
                    }
                    Build.VERSION.SDK_INT > Build.VERSION_CODES.P -> {
                        uriToFileQ(context, uri)?.absolutePath
                    }
                    else -> {
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            java.lang.Long.valueOf(documentId)
                        )
                        getDataColumn(context, contentUri, null, null)
                    }
                }

            } else if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            }
        } else if ("content".equals(uri.scheme!!, ignoreCase = true)) {
            // 如果是 content 类型的 Uri
            filePath = getDataColumn(context, uri, null, null)
        } else if ("file" == uri.scheme) {
            // 如果是 file 类型的 Uri,直接获取图片对应的路径
            filePath = uri.path
        }
        return filePath
    }

    /**
     * 获取数据库表中的 _data 列，即返回Uri对应的文件路径
     */
    private fun getDataColumn(
        context: Context,
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var path: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(projection[0])
                path = cursor.getString(columnIndex)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            cursor?.close()
        }

        return path
    }


    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is MediaProvider
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is DownloadsProvider
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

}