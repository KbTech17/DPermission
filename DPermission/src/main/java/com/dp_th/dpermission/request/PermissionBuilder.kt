package com.dp_th.dpermission.request

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.dp_th.dpermission.DPermission
import com.dp_th.dpermission.callback.ExplainReasonCallback
import com.dp_th.dpermission.callback.ExplainReasonCallbackWithBeforeParam
import com.dp_th.dpermission.callback.ForwardToSettingsCallback
import com.dp_th.dpermission.callback.RequestCallback
import com.dp_th.dpermission.dialog.DefaultDialog
import com.dp_th.dpermission.dialog.RationaleDialog
import com.dp_th.dpermission.dialog.RationaleDialogFragment
import java.util.*

class PermissionBuilder(
    fragmentActivity: FragmentActivity?,
    fragment: Fragment?,
    normalPermissions: MutableSet<String>,
    specialPermissions: MutableSet<String>
) {

    lateinit var activity: FragmentActivity

    private var fragment: Fragment? = null
    private var lightColor = -1
    private var darkColor = -1
    private var originRequestOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    private val fragmentManager: FragmentManager
        get() {
            return fragment?.childFragmentManager ?: activity.supportFragmentManager
        }
    private val invisibleFragment: InvisibleFragment
        get() {
            val existedFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)
            return if (existedFragment != null) {
                existedFragment as InvisibleFragment
            } else {
                val invisibleFragment = InvisibleFragment()
                fragmentManager.beginTransaction()
                    .add(invisibleFragment, FRAGMENT_TAG)
                    .commitNowAllowingStateLoss()
                invisibleFragment
            }
        }

    @JvmField
    var currentDialog: Dialog? = null
    @JvmField
    var normalPermissions: MutableSet<String>
    @JvmField
    var specialPermissions: MutableSet<String>
    @JvmField
    var explainReasonBeforeRequest = false
    @JvmField
    var showDialogCalled = false
    @JvmField
    var permissionsWontRequest: MutableSet<String> = LinkedHashSet()
    @JvmField
    var grantedPermissions: MutableSet<String> = LinkedHashSet()
    @JvmField
    var deniedPermissions: MutableSet<String> = LinkedHashSet()
    @JvmField
    var permanentDeniedPermissions: MutableSet<String> = LinkedHashSet()
    @JvmField
    var tempPermanentDeniedPermissions: MutableSet<String> = LinkedHashSet()

    @JvmField
    var tempReadMediaPermissions: MutableSet<String> = LinkedHashSet()
    @JvmField
    var forwardPermissions: MutableSet<String> = LinkedHashSet()
    @JvmField
    var requestCallback: RequestCallback? = null
    @JvmField
    var explainReasonCallback: ExplainReasonCallback? = null
    @JvmField
    var explainReasonCallbackWithBeforeParam: ExplainReasonCallbackWithBeforeParam? = null
    @JvmField
    var forwardToSettingsCallback: ForwardToSettingsCallback? = null
    val targetSdkVersion: Int
        get() = activity.applicationInfo.targetSdkVersion
    fun onExplainRequestReason(callback: ExplainReasonCallback?): PermissionBuilder {
        explainReasonCallback = callback
        return this
    }
    fun onExplainRequestReason(callback: ExplainReasonCallbackWithBeforeParam?): PermissionBuilder {
        explainReasonCallbackWithBeforeParam = callback
        return this
    }
    fun onForwardToSettings(callback: ForwardToSettingsCallback?): PermissionBuilder {
        forwardToSettingsCallback = callback
        return this
    }
    fun explainReasonBeforeRequest(): PermissionBuilder {
        explainReasonBeforeRequest = true
        return this
    }

    fun setDialogTintColor(lightColor: Int, darkColor: Int): PermissionBuilder {
        this.lightColor = lightColor
        this.darkColor = darkColor
        return this
    }

    fun request(callback: RequestCallback?) {
        requestCallback = callback
        startRequest()
    }

    fun showHandlePermissionDialog(
        chainTask: ChainTask,
        showReasonOrGoSettings: Boolean,
        permissions: List<String>,
        message: String,
        positiveText: String,
        negativeText: String?
    ) {
        val defaultDialog = DefaultDialog(
            activity,
            permissions,
            message,
            positiveText,
            negativeText,
            lightColor,
            darkColor
        )
        showHandlePermissionDialog(chainTask, showReasonOrGoSettings, defaultDialog)
    }

    fun showHandlePermissionDialog(
        chainTask: ChainTask,
        showReasonOrGoSettings: Boolean,
        dialog: RationaleDialog
    ) {
        showDialogCalled = true
        val permissions = dialog.permissionsToRequest
        if (permissions.isEmpty()) {
            chainTask.finish()
            return
        }
        currentDialog = dialog
        dialog.show()
        if (dialog is DefaultDialog && dialog.isPermissionLayoutEmpty()) {
            // No valid permission to show on the dialog.
            // We call dismiss instead.
            dialog.dismiss()
            chainTask.finish()
        }
        val positiveButton = dialog.positiveButton
        val negativeButton = dialog.negativeButton
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        positiveButton.isClickable = true
        positiveButton.setOnClickListener {
            dialog.dismiss()
            if (showReasonOrGoSettings) {
                chainTask.requestAgain(permissions)
            } else {
                forwardToSettings(permissions)
            }
        }
        if (negativeButton != null) {
            negativeButton.isClickable = true
            negativeButton.setOnClickListener {
                dialog.dismiss()
                chainTask.finish()
            }
        }
        currentDialog?.setOnDismissListener {
            currentDialog = null
        }
    }

    fun showHandlePermissionDialog(
        chainTask: ChainTask,
        showReasonOrGoSettings: Boolean,
        dialogFragment: RationaleDialogFragment
    ) {
        showDialogCalled = true
        val permissions = dialogFragment.permissionsToRequest
        if (permissions.isEmpty()) {
            chainTask.finish()
            return
        }
        dialogFragment.showNow(fragmentManager, "DPermissionRationaleDialogFragment")
        val positiveButton = dialogFragment.positiveButton
        val negativeButton = dialogFragment.negativeButton
        dialogFragment.isCancelable = false
        positiveButton.isClickable = true
        positiveButton.setOnClickListener {
            dialogFragment.dismiss()
            if (showReasonOrGoSettings) {
                chainTask.requestAgain(permissions)
            } else {
                forwardToSettings(permissions)
            }
        }
        if (negativeButton != null) {
            negativeButton.isClickable = true
            negativeButton.setOnClickListener(View.OnClickListener {
                dialogFragment.dismiss()
                chainTask.finish()
            })
        }
    }

    fun requestNow(permissions: Set<String>, chainTask: ChainTask) {
        invisibleFragment.requestNow(this, permissions, chainTask)
    }

    fun requestAccessBackgroundLocationPermissionNow(chainTask: ChainTask) {
        invisibleFragment.requestAccessBackgroundLocationPermissionNow(this, chainTask)
    }

    fun requestSystemAlertWindowPermissionNow(chainTask: ChainTask) {
        invisibleFragment.requestSystemAlertWindowPermissionNow(this, chainTask)
    }

    fun requestWriteSettingsPermissionNow(chainTask: ChainTask) {
        invisibleFragment.requestWriteSettingsPermissionNow(this, chainTask)
    }

    fun requestManageExternalStoragePermissionNow(chainTask: ChainTask) {
        invisibleFragment.requestManageExternalStoragePermissionNow(this, chainTask)
    }

    fun requestInstallPackagePermissionNow(chainTask: ChainTask) {
        invisibleFragment.requestInstallPackagesPermissionNow(this, chainTask)
    }

    fun requestNotificationPermissionNow(chainTask: ChainTask) {
        invisibleFragment.requestNotificationPermissionNow(this, chainTask)
    }

    fun requestBodySensorsBackgroundPermissionNow(chainTask: ChainTask) {
        invisibleFragment.requestBodySensorsBackgroundPermissionNow(this, chainTask)
    }

    fun shouldRequestBackgroundLocationPermission(): Boolean {
        return specialPermissions.contains(RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION)
    }

    fun shouldRequestSystemAlertWindowPermission(): Boolean {
        return specialPermissions.contains(Manifest.permission.SYSTEM_ALERT_WINDOW)
    }

    fun shouldRequestWriteSettingsPermission(): Boolean {
        return specialPermissions.contains(Manifest.permission.WRITE_SETTINGS)
    }

    fun shouldRequestManageExternalStoragePermission(): Boolean {
        return specialPermissions.contains(RequestManageExternalStoragePermission.MANAGE_EXTERNAL_STORAGE)
    }

    fun shouldRequestInstallPackagesPermission(): Boolean {
        return specialPermissions.contains(RequestInstallPackagesPermission.REQUEST_INSTALL_PACKAGES)
    }

    fun shouldRequestNotificationPermission(): Boolean {
        return specialPermissions.contains(DPermission.permission.POST_NOTIFICATIONS)
    }

    fun shouldRequestBodySensorsBackgroundPermission(): Boolean {
        return specialPermissions.contains(RequestBodySensorsBackgroundPermission.BODY_SENSORS_BACKGROUND)
    }

    private fun startRequest() {
        // Lock the orientation when requesting permissions, or callback maybe missed due to
        // activity destroyed.
        lockOrientation()

        // Build the request chain. RequestNormalPermissions runs first, then RequestBackgroundLocationPermission runs.
        val requestChain = RequestChain()
        requestChain.addTaskToChain(RequestNormalPermissions(this))
        requestChain.addTaskToChain(RequestBackgroundLocationPermission(this))
        requestChain.addTaskToChain(RequestSystemAlertWindowPermission(this))
        requestChain.addTaskToChain(RequestWriteSettingsPermission(this))
        requestChain.addTaskToChain(RequestManageExternalStoragePermission(this))
        requestChain.addTaskToChain(RequestInstallPackagesPermission(this))
        requestChain.addTaskToChain(RequestNotificationPermission(this))
        requestChain.addTaskToChain(RequestBodySensorsBackgroundPermission(this))
        requestChain.runTask()
    }

    private fun removeInvisibleFragment() {
        val existedFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)
        if (existedFragment != null) {
            fragmentManager.beginTransaction().remove(existedFragment).commitNowAllowingStateLoss()
        }
    }

    private fun restoreOrientation() {
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            activity.requestedOrientation = originRequestOrientation
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun lockOrientation() {
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            originRequestOrientation = activity.requestedOrientation
            val orientation = activity.resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            }
        }
    }

    private fun forwardToSettings(permissions: List<String>) {
        forwardPermissions.clear()
        forwardPermissions.addAll(permissions)
        invisibleFragment.forwardToSettings()
    }

    internal fun endRequest() {
        // Remove the InvisibleFragment from current Activity after request finished.
        removeInvisibleFragment()
        // Restore the orientation after request finished since it's locked before.
        restoreOrientation()
    }

    companion object {
        private const val FRAGMENT_TAG = "InvisibleFragment"
    }

    init {
        if (fragmentActivity != null) {
            activity = fragmentActivity
        }
        // activity and fragment must not be null at same time
        if (fragmentActivity == null && fragment != null) {
            activity = fragment.requireActivity()
        }
        this.fragment = fragment
        this.normalPermissions = normalPermissions
        this.specialPermissions = specialPermissions
    }
}