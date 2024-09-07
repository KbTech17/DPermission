package com.dp_th.dpermission.request

import com.dp_th.dpermission.dialog.RationaleDialog
import kotlin.jvm.JvmOverloads
import com.dp_th.dpermission.dialog.RationaleDialogFragment

class ForwardScope internal constructor(
    private val pb: PermissionBuilder,
    private val chainTask: ChainTask
) {
    @JvmOverloads
    fun showForwardToSettingsDialog(permissions: List<String>, message: String, positiveText: String, negativeText: String? = null) {
        pb.showHandlePermissionDialog(chainTask, false, permissions, message, positiveText, negativeText)
    }

    fun showForwardToSettingsDialog(dialog: RationaleDialog) {
        pb.showHandlePermissionDialog(chainTask, false, dialog)
    }

    fun showForwardToSettingsDialog(dialogFragment: RationaleDialogFragment) {
        pb.showHandlePermissionDialog(chainTask, false, dialogFragment)
    }
}