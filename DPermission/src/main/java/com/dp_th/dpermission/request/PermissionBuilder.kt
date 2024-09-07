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
import com.dp_th.dpermission.callback.RequestReasonCallback
import com.dp_th.dpermission.callback.CallbackReasonBeforeParam
import com.dp_th.dpermission.callback.ManualSettingCallback
import com.dp_th.dpermission.callback.OnPermissionCallback
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
    var explainBeforeRequest = false
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
    var perCallback: OnPermissionCallback? = null
    @JvmField
    var requestReasonCallback: RequestReasonCallback? = null
    @JvmField
    var callbackReasonBeforeParam: CallbackReasonBeforeParam? = null
    @JvmField
    var manualSettingCallback: ManualSettingCallback? = null
    val targetSdkVersion: Int
        get() = activity.applicationInfo.targetSdkVersion
    fun onReasonToRequest(callback: RequestReasonCallback?): PermissionBuilder {
        requestReasonCallback = callback
        return this
    }
    fun onReasonToRequest(callback: CallbackReasonBeforeParam?): PermissionBuilder {
        callbackReasonBeforeParam = callback
        return this
    }
    fun onManualSettings(callback: ManualSettingCallback?): PermissionBuilder {
        manualSettingCallback = callback
        return this
    }
    fun explainBeforeRequest(): PermissionBuilder {
        explainBeforeRequest = true
        return this
    }

    fun setDialogTintColor(lightColor: Int, darkColor: Int): PermissionBuilder {
        this.lightColor = lightColor
        this.darkColor = darkColor
        return this
    }

    fun request(callback: OnPermissionCallback?) {
        perCallback = callback
        startRequest()
    }

    fun showHandlePermissionDialog(
        reasonTask: ReasonTask,
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
        showHandlePermissionDialog(reasonTask, showReasonOrGoSettings, defaultDialog)
    }

    fun showHandlePermissionDialog(
        reasonTask: ReasonTask,
        showReasonOrGoSettings: Boolean,
        dialog: RationaleDialog
    ) {
        showDialogCalled = true
        val permissions = dialog.permissionsToRequest
        if (permissions.isEmpty()) {
            reasonTask.finish()
            return
        }
        currentDialog = dialog
        dialog.show()
        if (dialog is DefaultDialog && dialog.isPermissionLayoutEmpty()) {
            dialog.dismiss()
            reasonTask.finish()
        }
        val positiveButton = dialog.positiveButton
        val negativeButton = dialog.negativeButton
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        positiveButton.isClickable = true
        positiveButton.setOnClickListener {
            dialog.dismiss()
            if (showReasonOrGoSettings) {
                reasonTask.requestAgain(permissions)
            } else {
                forwardToSettings(permissions)
            }
        }
        if (negativeButton != null) {
            negativeButton.isClickable = true
            negativeButton.setOnClickListener {
                dialog.dismiss()
                reasonTask.finish()
            }
        }
        currentDialog?.setOnDismissListener {
            currentDialog = null
        }
    }

    fun showHandlePermissionDialog(
        reasonTask: ReasonTask,
        showReasonOrGoSettings: Boolean,
        dialogFragment: RationaleDialogFragment
    ) {
        showDialogCalled = true
        val permissions = dialogFragment.permissionsToRequest
        if (permissions.isEmpty()) {
            reasonTask.finish()
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
                reasonTask.requestAgain(permissions)
            } else {
                forwardToSettings(permissions)
            }
        }
        if (negativeButton != null) {
            negativeButton.isClickable = true
            negativeButton.setOnClickListener(View.OnClickListener {
                dialogFragment.dismiss()
                reasonTask.finish()
            })
        }
    }

    fun requestNow(permissions: Set<String>, reasonTask: ReasonTask) {
        invisibleFragment.requestNow(this, permissions, reasonTask)
    }

    fun requestAccessBackgroundLocationPermissionNow(reasonTask: ReasonTask) {
        invisibleFragment.requestAccessBackgroundLocationPermissionNow(this, reasonTask)
    }

    fun requestSystemAlertWindowPermissionNow(reasonTask: ReasonTask) {
        invisibleFragment.requestSystemAlertWindowPermissionNow(this, reasonTask)
    }

    fun requestWriteSettingsPermissionNow(reasonTask: ReasonTask) {
        invisibleFragment.requestWriteSettingsPermissionNow(this, reasonTask)
    }

    fun requestManageExternalStoragePermissionNow(reasonTask: ReasonTask) {
        invisibleFragment.requestManageExternalStoragePermissionNow(this, reasonTask)
    }

    fun requestInstallPackagePermissionNow(reasonTask: ReasonTask) {
        invisibleFragment.requestInstallPackagesPermissionNow(this, reasonTask)
    }

    fun requestNotificationPermissionNow(reasonTask: ReasonTask) {
        invisibleFragment.requestNotificationPermissionNow(this, reasonTask)
    }

    fun requestBodySensorsBackgroundPermissionNow(reasonTask: ReasonTask) {
        invisibleFragment.requestBodySensorsBackgroundPermissionNow(this, reasonTask)
    }

    fun shouldRequestBackgroundLocationPermission(): Boolean {
        return specialPermissions.contains(BackgroundLocationRequest.ACCESS_BACKGROUND_LOCATION)
    }

    fun shouldRequestSystemAlertWindowPermission(): Boolean {
        return specialPermissions.contains(Manifest.permission.SYSTEM_ALERT_WINDOW)
    }

    fun shouldRequestWriteSettingsPermission(): Boolean {
        return specialPermissions.contains(Manifest.permission.WRITE_SETTINGS)
    }

    fun shouldRequestManageExternalStoragePermission(): Boolean {
        return specialPermissions.contains(PermissionManageExternalStorage.MANAGE_EXTERNAL_STORAGE)
    }

    fun shouldRequestInstallPackagesPermission(): Boolean {
        return specialPermissions.contains(PermissionInstallPackages.REQUEST_INSTALL_PACKAGES)
    }

    fun shouldRequestNotificationPermission(): Boolean {
        return specialPermissions.contains(DPermission.permission.POST_NOTIFICATIONS)
    }

    fun shouldRequestBodySensorsBackgroundPermission(): Boolean {
        return specialPermissions.contains(BackgroundBodySensorRequest.BODY_SENSORS_BACKGROUND)
    }

    private fun startRequest() {
        lockOrientation()

        val requestChain = RequestChain()
        requestChain.addTaskToChain(PermissionNormal(this))
        requestChain.addTaskToChain(BackgroundLocationRequest(this))
        requestChain.addTaskToChain(PermissionSystemAlertWindow(this))
        requestChain.addTaskToChain(PermissionWriteSettings(this))
        requestChain.addTaskToChain(PermissionManageExternalStorage(this))
        requestChain.addTaskToChain(PermissionInstallPackages(this))
        requestChain.addTaskToChain(PermissionNotification(this))
        requestChain.addTaskToChain(BackgroundBodySensorRequest(this))
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