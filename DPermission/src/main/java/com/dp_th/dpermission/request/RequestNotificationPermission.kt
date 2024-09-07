package com.dp_th.dpermission.request

import com.dp_th.dpermission.DPermission

internal class RequestNotificationPermission internal constructor(permissionBuilder: PermissionBuilder)
    : BaseTask(permissionBuilder) {

    override fun request() {
        if (pb.shouldRequestNotificationPermission()) {
            if (DPermission.areNotificationsEnabled(pb.activity)) {
                // notification permission has already granted, we can finish this task now.
                finish()
                return
            }
            if (pb.explainReasonCallback != null || pb.explainReasonCallbackWithBeforeParam != null) {
                val requestList = mutableListOf(DPermission.permission.POST_NOTIFICATIONS)
                if (pb.explainReasonCallbackWithBeforeParam != null) {
                    // callback ExplainReasonCallbackWithBeforeParam prior to ExplainReasonCallback
                    pb.explainReasonCallbackWithBeforeParam!!.onExplainReason(explainScope, requestList, true)
                } else {
                    pb.explainReasonCallback!!.onExplainReason(explainScope, requestList)
                }
                return
            }
        }
        // Shouldn't request notification at this time, so we call finish() to finish this task.
        finish()
    }

    override fun requestAgain(permissions: List<String>) {
        // Don't care what the permissions param is, always request notification permission.
        pb.requestNotificationPermissionNow(this)
    }
}