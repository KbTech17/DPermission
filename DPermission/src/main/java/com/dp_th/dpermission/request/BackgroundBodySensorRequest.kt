package com.dp_th.dpermission.request

import android.Manifest
import android.os.Build
import com.dp_th.dpermission.DPermission

internal class BackgroundBodySensorRequest internal constructor(permissionBuilder: PermissionBuilder)
    : BaseTask(permissionBuilder) {

    override fun request() {
        if (pb.shouldRequestBodySensorsBackgroundPermission()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                // If app runs under Android T, there's no BODY_SENSORS_BACKGROUND permissions.
                // We remove it from request list, but will append it to the request callback as denied permission.
                pb.specialPermissions.remove(BODY_SENSORS_BACKGROUND)
                pb.permissionsWontRequest.add(BODY_SENSORS_BACKGROUND)
                finish()
                return
            }
            if (DPermission.isGranted(pb.activity, BODY_SENSORS_BACKGROUND)) {
                // BODY_SENSORS_BACKGROUND has already granted, we can finish this task now.
                finish()
                return
            }
            val bodySensorGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                DPermission.isGranted(pb.activity, Manifest.permission.BODY_SENSORS)
            } else {
                false
            }
            if (bodySensorGranted) {
                if (pb.requestReasonCallback != null || pb.callbackReasonBeforeParam != null) {
                    val requestList = mutableListOf(BODY_SENSORS_BACKGROUND)
                    if (pb.callbackReasonBeforeParam != null) {
                        // callback ExplainReasonCallbackWithBeforeParam prior to ExplainReasonCallback
                        pb.callbackReasonBeforeParam!!.onExplainReason(explainScope, requestList, true)
                    } else {
                        pb.requestReasonCallback!!.onExplainReason(explainScope, requestList)
                    }
                } else {
                    // No implementation of explainReasonCallback, so we have to request BODY_SENSORS_BACKGROUND without explanation.
                    requestAgain(emptyList())
                }
                return
            }
        }
        // Shouldn't request BODY_SENSORS_BACKGROUND at this time, so we call finish() to finish this task.
        finish()
    }

    override fun requestAgain(permissions: List<String>) {
        // Don't care what the permissions param is, always request BODY_SENSORS_BACKGROUND.
        pb.requestBodySensorsBackgroundPermissionNow(this)
    }

    companion object {
        const val BODY_SENSORS_BACKGROUND = "android.permission.BODY_SENSORS_BACKGROUND"
    }
}