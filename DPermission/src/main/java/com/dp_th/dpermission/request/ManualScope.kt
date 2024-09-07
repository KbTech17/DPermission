package com.dp_th.dpermission.request

import com.dp_th.dpermission.dialog.RationaleDialog
import kotlin.jvm.JvmOverloads
import com.dp_th.dpermission.dialog.RationaleDialogFragment

class ManualScope internal constructor(
    private val pb: PermissionBuilder,
    private val reasonTask: ReasonTask
) {
    @JvmOverloads
    fun showManualSettingDialog(permissions: List<String>, info: String, positive: String, negative: String? = null) {
        pb.showHandlePermissionDialog(reasonTask, false, permissions, info, positive, negative)
    }

    fun showManualSettingDialog(dialog: RationaleDialog) {
        pb.showHandlePermissionDialog(reasonTask, false, dialog)
    }

    fun showManualSettingDialog(dialogFragment: RationaleDialogFragment) {
        pb.showHandlePermissionDialog(reasonTask, false, dialogFragment)
    }
}