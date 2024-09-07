package com.dp_th.dpermission.request

import android.Manifest
import android.os.Build
import android.provider.Settings

internal class PermissionSystemAlertWindow internal constructor(permissionBuilder: PermissionBuilder) :
    BaseTask(permissionBuilder) {

    override fun request() {
        if (pb.shouldRequestSystemAlertWindowPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && pb.targetSdkVersion >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(pb.activity)) {
                    // SYSTEM_ALERT_WINDOW permission has already granted, we can finish this task now.
                    finish()
                    return
                }
                if (pb.requestReasonCallback != null || pb.callbackReasonBeforeParam != null) {
                    val requestList = mutableListOf(Manifest.permission.SYSTEM_ALERT_WINDOW)
                    if (pb.callbackReasonBeforeParam != null) {
                        // callback ExplainReasonCallbackWithBeforeParam prior to ExplainReasonCallback
                        pb.callbackReasonBeforeParam!!.onExplainReason(explainScope, requestList, true)
                    } else {
                        pb.requestReasonCallback!!.onExplainReason(explainScope, requestList)
                    }
                } else {
                    // No implementation of explainReasonCallback, we can't request
                    // SYSTEM_ALERT_WINDOW permission at this time, because user won't understand why.
                    finish()
                }
            } else {
                // SYSTEM_ALERT_WINDOW permission is automatically granted below Android M.
                pb.grantedPermissions.add(Manifest.permission.SYSTEM_ALERT_WINDOW)
                // At this time, SYSTEM_ALERT_WINDOW permission shouldn't be special treated anymore.
                pb.specialPermissions.remove(Manifest.permission.SYSTEM_ALERT_WINDOW)
                finish()
            }
        } else {
            // shouldn't request SYSTEM_ALERT_WINDOW permission at this time, so we call finish() to finish this task.
            finish()
        }
    }

    override fun requestAgain(permissions: List<String>) {
        // don't care what the permissions param is, always request SYSTEM_ALERT_WINDOW permission.
        pb.requestSystemAlertWindowPermissionNow(this)
    }
}