package com.dp_th.dpermission.request

import com.dp_th.dpermission.dialog.RationaleDialog
import com.dp_th.dpermission.dialog.RationaleDialogFragment


class ReasonToAsk internal constructor(
    private val pb: PermissionBuilder,
    private val reasonTask: ReasonTask
) {
    @JvmOverloads
    fun showReasonDialog(permissions: List<String>, message: String, positiveText: String, negativeText: String? = null) {
        pb.showHandlePermissionDialog(reasonTask, true, permissions, message, positiveText, negativeText)
    }

    fun showReasonDialog(dialog: RationaleDialog) {
        pb.showHandlePermissionDialog(reasonTask, true, dialog)
    }

    fun showReasonDialog(dialogFragment: RationaleDialogFragment) {
        pb.showHandlePermissionDialog(reasonTask, true, dialogFragment)
    }

}