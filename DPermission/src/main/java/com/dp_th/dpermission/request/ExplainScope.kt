package com.dp_th.dpermission.request

import com.dp_th.dpermission.dialog.RationaleDialog
import kotlin.jvm.JvmOverloads
import com.dp_th.dpermission.dialog.RationaleDialogFragment

class ExplainScope internal constructor(
    private val pb: PermissionBuilder,
    private val chainTask: ChainTask
) {
    @JvmOverloads
    fun showRequestReasonDialog(permissions: List<String>, message: String, positiveText: String, negativeText: String? = null) {
        pb.showHandlePermissionDialog(chainTask, true, permissions, message, positiveText, negativeText)
    }

    fun showRequestReasonDialog(dialog: RationaleDialog) {
        pb.showHandlePermissionDialog(chainTask, true, dialog)
    }

    fun showRequestReasonDialog(dialogFragment: RationaleDialogFragment) {
        pb.showHandlePermissionDialog(chainTask, true, dialogFragment)
    }
}