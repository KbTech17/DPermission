package com.dp_th.dpermission.request

import com.dp_th.dpermission.DPermission

internal class PermissionNotification internal constructor(permissionBuilder: PermissionBuilder)
    : BaseTask(permissionBuilder) {

    override fun request() {
        if (pb.shouldRequestNotificationPermission()) {
            if (DPermission.isNotificationEnabled(pb.activity)) {
                finish()
                return
            }
            if (pb.requestReasonCallback != null || pb.callbackReasonBeforeParam != null) {
                val requestList = mutableListOf(DPermission.permission.POST_NOTIFICATIONS)
                if (pb.callbackReasonBeforeParam != null) {
                    pb.callbackReasonBeforeParam!!.onExplainReason(explainScope, requestList, true)
                } else {
                    pb.requestReasonCallback!!.onExplainReason(explainScope, requestList)
                }
                return
            }
        }
        finish()
    }

    override fun requestAgain(permissions: List<String>) {
        pb.requestNotificationPermissionNow(this)
    }
}