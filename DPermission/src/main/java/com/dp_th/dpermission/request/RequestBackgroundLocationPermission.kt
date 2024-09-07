package com.dp_th.dpermission.request

import android.Manifest
import android.os.Build
import com.dp_th.dpermission.DPermission

internal class RequestBackgroundLocationPermission internal constructor(permissionBuilder: PermissionBuilder)
    : BaseTask(permissionBuilder) {

    override fun request() {
        if (pb.shouldRequestBackgroundLocationPermission()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                // If app runs under Android Q, there's no ACCESS_BACKGROUND_LOCATION permissions.
                // We remove it from request list, but will append it to the request callback as denied permission.
                pb.specialPermissions.remove(ACCESS_BACKGROUND_LOCATION)
                pb.permissionsWontRequest.add(ACCESS_BACKGROUND_LOCATION)
                finish()
                return
            }
            if (DPermission.isGranted(pb.activity, ACCESS_BACKGROUND_LOCATION)) {
                // ACCESS_BACKGROUND_LOCATION has already granted, we can finish this task now.
                finish()
                return
            }
            val accessFindLocationGranted = DPermission.isGranted(pb.activity, Manifest.permission.ACCESS_FINE_LOCATION)
            val accessCoarseLocationGranted = DPermission.isGranted(pb.activity, Manifest.permission.ACCESS_COARSE_LOCATION)
            if (accessFindLocationGranted || accessCoarseLocationGranted) {
                if (pb.explainReasonCallback != null || pb.explainReasonCallbackWithBeforeParam != null) {
                    val requestList = mutableListOf(ACCESS_BACKGROUND_LOCATION)
                    if (pb.explainReasonCallbackWithBeforeParam != null) {
                        // callback ExplainReasonCallbackWithBeforeParam prior to ExplainReasonCallback
                        pb.explainReasonCallbackWithBeforeParam!!.onExplainReason(explainScope, requestList, true)
                    } else {
                        pb.explainReasonCallback!!.onExplainReason(explainScope, requestList)
                    }
                } else {
                    // No implementation of explainReasonCallback, so we have to request ACCESS_BACKGROUND_LOCATION without explanation.
                    requestAgain(emptyList())
                }
                return
            }
        }
        // Shouldn't request ACCESS_BACKGROUND_LOCATION at this time, so we call finish() to finish this task.
        finish()
    }

    override fun requestAgain(permissions: List<String>) {
        // Don't care what the permissions param is, always request ACCESS_BACKGROUND_LOCATION.
        pb.requestAccessBackgroundLocationPermissionNow(this)
    }

    companion object {
        const val ACCESS_BACKGROUND_LOCATION = "android.permission.ACCESS_BACKGROUND_LOCATION"
    }
}