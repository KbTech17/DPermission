package com.dp_th.dpermission.request

import android.Manifest
import android.os.Build
import android.os.Environment
import android.provider.Settings
import com.dp_th.dpermission.DPermission
import java.util.*

internal abstract class BaseTask(@JvmField var pb: PermissionBuilder) :
    ReasonTask {
    @JvmField
    var next: ReasonTask? = null
    private var explainReasonScope = ReasonToAsk(pb, this)
    private var forwardToSettingsScope = ManualScope(pb, this)

    override fun getExplainScope() = explainReasonScope

    override fun getForwardScope() = forwardToSettingsScope

    override fun finish() {
        // If there's next task, then run it.
        next?.request() ?: run {
            // If there's no next task, finish the request process and notify the result
            val deniedList: MutableList<String> = ArrayList()
            deniedList.addAll(pb.deniedPermissions)
            deniedList.addAll(pb.permanentDeniedPermissions)
            deniedList.addAll(pb.permissionsWontRequest)
            if (pb.shouldRequestBackgroundLocationPermission()) {
                if (DPermission.isGranted(pb.activity,
                        RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION
                    )) {
                    pb.grantedPermissions.add(RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    deniedList.add(RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION)
                }
            }
            if (pb.shouldRequestSystemAlertWindowPermission()
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && pb.targetSdkVersion >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(pb.activity)) {
                    pb.grantedPermissions.add(Manifest.permission.SYSTEM_ALERT_WINDOW)
                } else {
                    deniedList.add(Manifest.permission.SYSTEM_ALERT_WINDOW)
                }
            }
            if (pb.shouldRequestWriteSettingsPermission()
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && pb.targetSdkVersion >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(pb.activity)) {
                    pb.grantedPermissions.add(Manifest.permission.WRITE_SETTINGS)
                } else {
                    deniedList.add(Manifest.permission.WRITE_SETTINGS)
                }
            }
            if (pb.shouldRequestManageExternalStoragePermission()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                    Environment.isExternalStorageManager()) {
                    pb.grantedPermissions.add(RequestManageExternalStoragePermission.MANAGE_EXTERNAL_STORAGE)
                } else {
                    deniedList.add(RequestManageExternalStoragePermission.MANAGE_EXTERNAL_STORAGE)
                }
            }
            if (pb.shouldRequestInstallPackagesPermission()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && pb.targetSdkVersion >= Build.VERSION_CODES.O) {
                    if (pb.activity.packageManager.canRequestPackageInstalls()) {
                        pb.grantedPermissions.add(RequestInstallPackagesPermission.REQUEST_INSTALL_PACKAGES)
                    } else {
                        deniedList.add(RequestInstallPackagesPermission.REQUEST_INSTALL_PACKAGES)
                    }
                } else {
                    deniedList.add(RequestInstallPackagesPermission.REQUEST_INSTALL_PACKAGES)
                }
            }
            if (pb.shouldRequestNotificationPermission()) {
                if (DPermission.areNotificationsEnabled(pb.activity)) {
                    pb.grantedPermissions.add(DPermission.permission.POST_NOTIFICATIONS)
                } else {
                    deniedList.add(DPermission.permission.POST_NOTIFICATIONS)
                }
            }
            if (pb.shouldRequestBodySensorsBackgroundPermission()) {
                if (DPermission.isGranted(pb.activity,
                        RequestBodySensorsBackgroundPermission.BODY_SENSORS_BACKGROUND
                    )) {
                    pb.grantedPermissions.add(RequestBodySensorsBackgroundPermission.BODY_SENSORS_BACKGROUND)
                } else {
                    deniedList.add(RequestBodySensorsBackgroundPermission.BODY_SENSORS_BACKGROUND)
                }
            }
            if (pb.perCallback != null) {
                pb.perCallback!!.onPermissionResult(deniedList.isEmpty(), ArrayList(pb.grantedPermissions), deniedList)
            }

            pb.endRequest()
        }
    }

    init {
        explainReasonScope = ReasonToAsk(pb, this)
        forwardToSettingsScope = ManualScope(pb, this)
    }
}