package com.tech.mvpframework.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.tech.mvpframework.R
import com.tech.mvpframework.utils.CommonUtils


object PermissionUtil {

    private var sPermissions: ArrayList<String>? = null
    private var sMustPermissions: ArrayList<String>? = null
    private var listen: PermissionUtilListen? = null
    private var sTempHavePermissions: ArrayList<String>? = null  // 用于回调
    private var firstCheckMust = false
    private var firstCheck = false

    /**
     * @param permission 非必须权限
     * @param mustPermission 必须权限
     */
    public fun requestRuntimePermissions(
        c: Context,
        permission: ArrayList<String>,
        mustPermission: ArrayList<String>,
        listen: PermissionUtilListen
    ) {
        firstCheckMust = false
        firstCheck = false
        sPermissions = permission
        sMustPermissions = mustPermission
        this.listen = listen
        requestPermissions(c, permission, mustPermission)
    }

    private fun requestPermissions(
        c: Context, permission: ArrayList<String>,
        mustPermission: ArrayList<String>
    ) {
        // 没有允许的必须权限
        val mustPermissionList = ArrayList<String>()

        for (i in mustPermission) {
            if (ContextCompat.checkSelfPermission(c, i) != PackageManager.PERMISSION_GRANTED) {
                mustPermissionList.add(i)
            }
        }

        // 如果必须权限不为空，且存在拒接必须先跳去设置的对话框
        if (mustPermissionList.isNotEmpty()) {
            val refusePermissionList = ArrayList<String>()
            for (i in mustPermissionList) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(c as Activity, i)) {
                    refusePermissionList.add(i)
                }
            }

            if (refusePermissionList.isNotEmpty() && firstCheckMust) {
                showDialogMust(c, refusePermissionList[0])
                return
            } else {
                firstCheckMust = true
            }
        }

        // 没有允许的非必要权限
        val permissionList = ArrayList<String>()

        for (i in permission) {
            if (ContextCompat.checkSelfPermission(c, i) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(i)
            }
        }

        // 没有必要权限，允许拒接
        if (mustPermissionList.isEmpty()) {
            val refusePermissionList = ArrayList<String>()
            val noRefusePermissionList = ArrayList<String>()

            // 检查有没有拒接的非必要权限
            for (i in permissionList) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(c as Activity, i)) {
                    refusePermissionList.add(i)
                } else {
                    noRefusePermissionList.add(i)
                }
            }

            // 如果存在拒接的非必要权限
            if (refusePermissionList.isNotEmpty() && firstCheck) {
                sTempHavePermissions = ArrayList()
                sTempHavePermissions!!.addAll(noRefusePermissionList)
                sMustPermissions?.let { sTempHavePermissions!!.addAll(it) }
                showDialog(c, refusePermissionList[0])
                firstCheck = true
            } else {
                if (permissionList.isEmpty()) {
                    val allPermission = ArrayList<String>()
                    sMustPermissions?.let { allPermission.addAll(it) }
                    sPermissions?.let { allPermission.addAll(it) }
                    this.listen?.result(allPermission)
                    clearData()
                } else {
                    ActivityCompat.requestPermissions(
                        c as Activity,
                        permissionList.toTypedArray(),
                        2222
                    )
                }
            }
        } else {
            val allPermission = ArrayList<String>()
            allPermission.addAll(mustPermissionList)
            allPermission.addAll(permissionList)
            ActivityCompat.requestPermissions(
                c as Activity,
                allPermission.toTypedArray(),
                1111
            )
        }
    }

    private fun requestPermissions(context: Context, permissions: ArrayList<String>) {
        val permissionList = java.util.ArrayList<String>() // 已有需要请求非必权限

        // 判断是否有权限
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                permissionList.add(permission)
            }
        }

        sMustPermissions?.let { permissionList.addAll(it) }
        listen?.result(permissionList)
        clearData()
    }


    public fun onRequestPermissionsResult(
        context: Context,
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1111) {
            requestPermissions(
                context,
                sPermissions ?: ArrayList<String>(),
                sMustPermissions ?: ArrayList<String>()
            )
        } else if (requestCode == 2222) {
            requestPermissions(context, sPermissions ?: ArrayList<String>())
        }
    }

    private fun clearData() {
        openSet = false
        sPermissions = null
        sMustPermissions = null
        sTempHavePermissions = null
        listen = null
    }

    // 必要权限去设置页
    private fun showDialogMust(c: Context, p: String) {
        val normalDialog = AlertDialog.Builder(c, R.style.DefaultDialogTheme)
        normalDialog.setCancelable(false)
        normalDialog.setTitle(c.getString(R.string.permission_tip))
        when (p) {
            Manifest.permission.ACCESS_FINE_LOCATION -> {
                normalDialog.setMessage(c.getString(R.string.permission_position))
            }
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE -> {
                normalDialog.setMessage(c.getString(R.string.permission_photo))
            }
            Manifest.permission.RECORD_AUDIO -> {
                normalDialog.setMessage(c.getString(R.string.permission_voice))
            }
            Manifest.permission.CAMERA -> {
                normalDialog.setMessage(c.getString(R.string.permission_camera))
            }
            else -> {
                normalDialog.setMessage("In order to guarantee your normal use of Meete, please allow us relevant permissions.")
            }
        }
        normalDialog.setPositiveButton(
            c.getText(R.string.permission_goto_set),
            object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    gotoSetPage(c, true)
                }
            })
        normalDialog.setNegativeButton(
            c.getText(R.string.permission_no_set),
            object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    listen?.cancel()
                    clearData()
                }
            })
        // 显示
        normalDialog.show()
    }

    // 非必要权限去设置页
    private fun showDialog(c: Context, p: String) {
        val normalDialog = AlertDialog.Builder(c, R.style.DefaultDialogTheme)
        normalDialog.setTitle(c.getString(R.string.permission_tip))
        when (p) {
            Manifest.permission.ACCESS_FINE_LOCATION -> {
                normalDialog.setMessage(c.getString(R.string.permission_position))
            }
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE -> {
                normalDialog.setMessage(c.getString(R.string.permission_photo))
            }
            Manifest.permission.RECORD_AUDIO -> {
                normalDialog.setMessage(c.getString(R.string.permission_voice))
            }
            Manifest.permission.CAMERA -> {
                normalDialog.setMessage(c.getString(R.string.permission_camera))
            }
            else -> {
                normalDialog.setMessage("In order to guarantee your normal use of Meete, please allow us relevant permissions.")
            }
        }
        normalDialog.setPositiveButton(
            c.getText(R.string.permission_goto_set),
            object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    gotoSetPage(c, false)
                }
            })
        normalDialog.setNegativeButton(
            c.getText(R.string.permission_no_set),
            object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    listen?.result(sTempHavePermissions!!)
                    clearData()
                }
            })
        // 显示
        normalDialog.show()
    }

    var openSet = false
    var openSetFromMust = true

    private fun gotoSetPage(context: Context, fromMust: Boolean) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", context.applicationContext.packageName, null)
        (context as Activity).startActivity(intent)
        openSetFromMust = fromMust
        openSet = true
    }

    fun onResume(c: Context) {
        if (openSet) {
            openSet = false
            if (openSetFromMust) {
                requestPermissions(
                    c,
                    sPermissions ?: ArrayList<String>(),
                    sMustPermissions ?: ArrayList<String>()
                )
            } else {
                requestPermissions(c, sPermissions ?: ArrayList<String>())
            }
        }
    }

    fun hasPushPermission(context: Context): Boolean {
        val notification = NotificationManagerCompat.from(context)
        return notification.areNotificationsEnabled()
    }

    fun gotoPushPermissionSetting(context: Context) {
        val intent = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra("android.provider.extra.APP_PACKAGE", CommonUtils.getPkgName(context))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
            intent.putExtra("app_package", CommonUtils.getPkgName(context))
            intent.putExtra("app_uid", CommonUtils.getUid(context))
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data = Uri.parse("package:" + CommonUtils.getPkgName(context))
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", CommonUtils.getPkgName(context), null)
        }
        context.startActivity(intent)
    }

    /**
     * 用于检查是否有拒接不在提示的权限
     */
    fun checkShouldShowRequestPremission(
        c: Context,
        permissions: ArrayList<String>,
        listen: PermissionUtilListen
    ) {
        val result = ArrayList<String>()
        for (p in permissions) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(c as Activity, p)) {
                result.add(p)
            }
        }
        listen.result(result)
    }

    /**
     * 显示去设置页对话框
     */
    fun showToSetDialog(c: Context, title: String, tip: String) {
        val normalDialog = AlertDialog.Builder(c, R.style.DefaultDialogTheme)
        normalDialog.setCancelable(false)
        normalDialog.setTitle(title)
        normalDialog.setMessage(tip)
        normalDialog.setPositiveButton(
            c.getText(R.string.permission_goto_set),
            object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    // 去设置页
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.data = Uri.fromParts("package", c.applicationContext.packageName, null)
                    (c as Activity).startActivity(intent)
                }
            })
        normalDialog.setNegativeButton(
            c.getText(R.string.permission_no_set),
            object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {

                }
            })
        // 显示
        normalDialog.show()
    }

    interface PermissionUtilListen {

        public fun cancel()

        // 参数 有权限
        public fun result(permissions: ArrayList<String>)
    }
}