package com.dp_th.dpermission.request

import android.Manifest
import android.os.Build

internal class PermissionInstallPackages internal constructor(permissionBuilder: PermissionBuilder) :
    BaseTask(permissionBuilder) {

    override fun request() {
        if (pb.shouldRequestInstallPackagesPermission()
            && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            && pb.targetSdkVersion >= Build.VERSION_CODES.O) {
            if (pb.activity.packageManager.canRequestPackageInstalls()) {
                // REQUEST_INSTALL_PACKAGES permission has already granted, we can finish this task now.
                finish()
                return
            }
            if (pb.requestReasonCallback != null || pb.callbackReasonBeforeParam != null) {
                val requestList = mutableListOf(Manifest.permission.REQUEST_INSTALL_PACKAGES)
                if (pb.callbackReasonBeforeParam != null) {
                    // callback ExplainReasonCallbackWithBeforeParam prior to ExplainReasonCallback
                    pb.callbackReasonBeforeParam!!.onExplainReason(explainScope, requestList, true)
                } else {
                    pb.requestReasonCallback!!.onExplainReason(explainScope, requestList)
                }
            } else {
                // No implementation of explainReasonCallback, we can't request
                // REQUEST_INSTALL_PACKAGES permission at this time, because user won't understand why.
                finish()
            }
        } else {
            // shouldn't request REQUEST_INSTALL_PACKAGES permission at this time, so we call finish() to finish this task.
            finish()
        }
    }

    override fun requestAgain(permissions: List<String>) {
        // don't care what the permissions param is, always request REQUEST_INSTALL_PACKAGES permission.
        pb.requestInstallPackagePermissionNow(this)
    }

    companion object {
        const val REQUEST_INSTALL_PACKAGES = "android.permission.REQUEST_INSTALL_PACKAGES"
    }
}