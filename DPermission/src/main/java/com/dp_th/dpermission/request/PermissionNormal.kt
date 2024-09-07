package com.dp_th.dpermission.request

import com.dp_th.dpermission.DPermission
import java.util.*

internal class PermissionNormal internal constructor(permissionBuilder: PermissionBuilder) :
    BaseTask(permissionBuilder) {

    override fun request() {
        val requestList = ArrayList<String>()
        for (permission in pb.normalPermissions) {
            if (DPermission.isGranted(pb.activity, permission)) {
                pb.grantedPermissions.add(permission) // already granted
            } else {
                requestList.add(permission) // still need to request
            }
        }
        if (requestList.isEmpty()) { // all permissions are granted
            finish()
            return
        }
        if (pb.explainBeforeRequest && (pb.requestReasonCallback != null || pb.callbackReasonBeforeParam != null)) {
            pb.explainBeforeRequest = false
            pb.deniedPermissions.addAll(requestList)
            if (pb.callbackReasonBeforeParam != null) {
                // callback ExplainReasonCallbackWithBeforeParam prior to ExplainReasonCallback
                pb.callbackReasonBeforeParam!!.onExplainReason(explainScope, requestList, true)
            } else {
                pb.requestReasonCallback!!.onExplainReason(explainScope, requestList)
            }
        } else {
            // Do the request at once. Always request all permissions no matter they are already granted or not, in case user turn them off in Settings.
            pb.requestNow(pb.normalPermissions, this)
        }
    }

    override fun requestAgain(permissions: List<String>) {
        val permissionsToRequestAgain: MutableSet<String> = HashSet(pb.grantedPermissions)
        permissionsToRequestAgain.addAll(permissions)
        if (permissionsToRequestAgain.isNotEmpty()) {
            pb.requestNow(permissionsToRequestAgain, this)
        } else {
            finish()
        }
    }
}