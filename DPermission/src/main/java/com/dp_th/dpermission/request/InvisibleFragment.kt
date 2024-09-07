package com.dp_th.dpermission.request

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.dp_th.dpermission.DPermission
import java.util.ArrayList

class InvisibleFragment : Fragment() {

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var pb: PermissionBuilder

    private lateinit var task: ChainTask

    private val requestNormalPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantResults ->
            postForResult {
                onRequestNormalPermissionsResult(grantResults)
            }
        }

    private val requestBackgroundLocationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            postForResult {
                onRequestBackgroundLocationPermissionResult(granted)
            }
        }

    private val requestSystemAlertWindowLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            postForResult {
                onRequestSystemAlertWindowPermissionResult()
            }
        }

    private val requestWriteSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            postForResult {
                onRequestWriteSettingsPermissionResult()
            }
        }

    private val requestManageExternalStorageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            postForResult {
                onRequestManageExternalStoragePermissionResult()
            }
        }

    private val requestInstallPackagesLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            postForResult {
                onRequestInstallPackagesPermissionResult()
            }
        }

    private val requestNotificationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            postForResult {
                onRequestNotificationPermissionResult()
            }
        }

    private val requestBodySensorsBackgroundLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            postForResult {
                onRequestBodySensorsBackgroundPermissionResult(granted)
            }
        }

    private val forwardToSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (checkForGC()) {
                task.requestAgain(ArrayList(pb.forwardPermissions))
            }
        }

    fun requestNow(
        permissionBuilder: PermissionBuilder,
        permissions: Set<String>,
        chainTask: ChainTask
    ) {
        pb = permissionBuilder
        task = chainTask
        requestNormalPermissionLauncher.launch(permissions.toTypedArray())
    }

    fun requestAccessBackgroundLocationPermissionNow(
        permissionBuilder: PermissionBuilder,
        chainTask: ChainTask
    ) {
        pb = permissionBuilder
        task = chainTask
        requestBackgroundLocationLauncher.launch(RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION)
    }

    fun requestSystemAlertWindowPermissionNow(
        permissionBuilder: PermissionBuilder,
        chainTask: ChainTask
    ) {
        pb = permissionBuilder
        task = chainTask
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(requireContext())) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:${requireActivity().packageName}")
            requestSystemAlertWindowLauncher.launch(intent)
        } else {
            onRequestSystemAlertWindowPermissionResult()
        }
    }

    fun requestWriteSettingsPermissionNow(
        permissionBuilder: PermissionBuilder,
        chainTask: ChainTask
    ) {
        pb = permissionBuilder
        task = chainTask
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(requireContext())) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:${requireActivity().packageName}")
            requestWriteSettingsLauncher.launch(intent)
        } else {
            onRequestWriteSettingsPermissionResult()
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun requestManageExternalStoragePermissionNow(
        permissionBuilder: PermissionBuilder,
        chainTask: ChainTask
    ) {
        pb = permissionBuilder
        task = chainTask
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            var intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:${requireActivity().packageName}")
            if (intent.resolveActivity(requireActivity().packageManager) == null) {
                intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            }
            requestManageExternalStorageLauncher.launch(intent)
        } else {
            onRequestManageExternalStoragePermissionResult()
        }
    }

    fun requestInstallPackagesPermissionNow(
        permissionBuilder: PermissionBuilder,
        chainTask: ChainTask
    ) {
        pb = permissionBuilder
        task = chainTask
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            intent.data = Uri.parse("package:${requireActivity().packageName}")
            requestInstallPackagesLauncher.launch(intent)
        } else {
            onRequestInstallPackagesPermissionResult()
        }
    }

    fun requestNotificationPermissionNow(
        permissionBuilder: PermissionBuilder,
        chainTask: ChainTask
    ) {
        pb = permissionBuilder
        task = chainTask
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireActivity().packageName)
            requestNotificationLauncher.launch(intent)
        } else {
            onRequestInstallPackagesPermissionResult()
        }
    }

    fun requestBodySensorsBackgroundPermissionNow(
        permissionBuilder: PermissionBuilder,
        chainTask: ChainTask
    ) {
        pb = permissionBuilder
        task = chainTask
        requestBodySensorsBackgroundLauncher.launch(RequestBodySensorsBackgroundPermission.BODY_SENSORS_BACKGROUND)
    }

    fun forwardToSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        forwardToSettingsLauncher.launch(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (checkForGC()) {
            // Dismiss the showing dialog when InvisibleFragment destroyed for avoiding window leak problem.
            pb.currentDialog?.let {
                if (it.isShowing) {
                    it.dismiss()
                }
            }
        }
    }

    private fun onRequestNormalPermissionsResult(grantResults: Map<String, Boolean>) {
        if (checkForGC()) {
            // We can never holds granted permissions for safety, because user may turn some permissions off in settings.
            // So every time request, must request the already granted permissions again and refresh the granted permission set.
            pb.grantedPermissions.clear()
            val showReasonList: MutableList<String> =
                ArrayList() // holds denied permissions in the request permissions.
            val forwardList: MutableList<String> =
                ArrayList() // hold permanently denied permissions in the request permissions.
            for ((permission, granted) in grantResults) {
                if (granted) {
                    pb.grantedPermissions.add(permission)
                    // Remove granted permissions from deniedPermissions and permanentDeniedPermissions set in PermissionBuilder.
                    pb.deniedPermissions.remove(permission)
                    pb.permanentDeniedPermissions.remove(permission)
                } else {
                    // Denied permission can turn into permanent denied permissions, but permanent denied permission can not turn into denied permissions.
                    val shouldShowRationale = shouldShowRequestPermissionRationale(permission)
                    if (shouldShowRationale) {
                        showReasonList.add(permission)
                        pb.deniedPermissions.add(permission)
                        // So there's no need to remove the current permission from permanentDeniedPermissions because it won't be there.
                    } else {
                        forwardList.add(permission)
                        pb.permanentDeniedPermissions.add(permission)
                        // We must remove the current permission from deniedPermissions because it is permanent denied permission now.
                        pb.deniedPermissions.remove(permission)
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (pb.grantedPermissions.contains(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)) {
                    if (pb.deniedPermissions.contains(Manifest.permission.READ_MEDIA_IMAGES)) {
                        pb.deniedPermissions.remove(Manifest.permission.READ_MEDIA_IMAGES)
                        showReasonList.remove(Manifest.permission.READ_MEDIA_IMAGES)
                        pb.tempReadMediaPermissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                    } else if (pb.permanentDeniedPermissions.contains(Manifest.permission.READ_MEDIA_IMAGES)) {
                        pb.permanentDeniedPermissions.remove(Manifest.permission.READ_MEDIA_IMAGES)
                        forwardList.remove(Manifest.permission.READ_MEDIA_IMAGES)
                        pb.tempReadMediaPermissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                    if (pb.deniedPermissions.contains(Manifest.permission.READ_MEDIA_VIDEO)) {
                        pb.deniedPermissions.remove(Manifest.permission.READ_MEDIA_VIDEO)
                        showReasonList.remove(Manifest.permission.READ_MEDIA_VIDEO)
                        pb.tempReadMediaPermissions.add(Manifest.permission.READ_MEDIA_VIDEO)
                    } else if (pb.permanentDeniedPermissions.contains(Manifest.permission.READ_MEDIA_VIDEO)) {
                        pb.permanentDeniedPermissions.remove(Manifest.permission.READ_MEDIA_VIDEO)
                        forwardList.remove(Manifest.permission.READ_MEDIA_VIDEO)
                        pb.tempReadMediaPermissions.add(Manifest.permission.READ_MEDIA_VIDEO)
                    }
                }
            }

            val deniedPermissions: MutableList<String> =
                ArrayList() // used to validate the deniedPermissions and permanentDeniedPermissions
            deniedPermissions.addAll(pb.deniedPermissions)
            deniedPermissions.addAll(pb.permanentDeniedPermissions)
            // maybe user can turn some permissions on in settings that we didn't request, so check the denied permissions again for safety.
            for (permission in deniedPermissions) {
                if (DPermission.isGranted(requireContext(), permission)) {
                    pb.deniedPermissions.remove(permission)
                    pb.grantedPermissions.add(permission)
                }
            }
            val isGranted = pb.grantedPermissions.size == pb.normalPermissions.size
            if (isGranted) { // If all permissions are granted, finish current task directly.
                task.finish()
            } else {
                var shouldFinishTheTask = true // Indicate if we should finish the task
                // If explainReasonCallback is not null and there are denied permissions. Try the ExplainReasonCallback.
                if ((pb.explainReasonCallback != null || pb.explainReasonCallbackWithBeforeParam != null) && showReasonList.isNotEmpty()) {
                    shouldFinishTheTask =
                        false // shouldn't because ExplainReasonCallback handles it
                    if (pb.explainReasonCallbackWithBeforeParam != null) {
                        // callback ExplainReasonCallbackWithBeforeParam prior to ExplainReasonCallback
                        pb.explainReasonCallbackWithBeforeParam!!.onExplainReason(
                            task.explainScope, ArrayList(pb.deniedPermissions), false
                        )
                    } else {
                        pb.explainReasonCallback!!.onExplainReason(
                            task.explainScope,
                            ArrayList(pb.deniedPermissions)
                        )
                    }
                    // store these permanently denied permissions or they will be lost when request again.
                    pb.tempPermanentDeniedPermissions.addAll(forwardList)
                } else if (pb.forwardToSettingsCallback != null && (forwardList.isNotEmpty() || pb.tempPermanentDeniedPermissions.isNotEmpty())) {
                    shouldFinishTheTask =
                        false // shouldn't because ForwardToSettingsCallback handles it
                    pb.tempPermanentDeniedPermissions.clear() // no need to store them anymore once onForwardToSettings callback.
                    pb.forwardToSettingsCallback!!.onForwardToSettings(
                        task.forwardScope,
                        ArrayList(pb.permanentDeniedPermissions)
                    )
                }
                // If showRequestReasonDialog or showForwardToSettingsDialog is not called. We should finish the task.
                // There's case that ExplainReasonCallback or ForwardToSettingsCallback is called, but developer didn't invoke
                // showRequestReasonDialog or showForwardToSettingsDialog in the callback.
                // At this case and all other cases, task should be finished.
                if (shouldFinishTheTask || !pb.showDialogCalled) {
                    for (tempReadMediaPermission in pb.tempReadMediaPermissions) {
                        pb.deniedPermissions.add(tempReadMediaPermission)
                    }
                    pb.tempReadMediaPermissions.clear()
                    task.finish()
                }
                // Reset this value after each request. If we don't do this, developer invoke showRequestReasonDialog in ExplainReasonCallback
                // but didn't invoke showForwardToSettingsDialog in ForwardToSettingsCallback, the request process will be lost. Because the
                // previous showDialogCalled affect the next request logic.
                pb.showDialogCalled = false
            }
        }
    }

    private fun onRequestBackgroundLocationPermissionResult(granted: Boolean) {
        if (checkForGC()) {
            postForResult {
                if (granted) {
                    pb.grantedPermissions.add(RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION)
                    // Remove granted permissions from deniedPermissions and permanentDeniedPermissions set in PermissionBuilder.
                    pb.deniedPermissions.remove(RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION)
                    pb.permanentDeniedPermissions.remove(RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION)
                    task.finish()
                } else {
                    var goesToOnPermissionCallback = true // Indicate if we should finish the task
                    val shouldShowRationale =
                        shouldShowRequestPermissionRationale(RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION)
                    // If explainReasonCallback is not null and we should show rationale. Try the ExplainReasonCallback.
                    if ((pb.explainReasonCallback != null || pb.explainReasonCallbackWithBeforeParam != null) && shouldShowRationale) {
                        goesToOnPermissionCallback =
                            false // shouldn't because ExplainReasonCallback handles it
                        val permissionsToExplain: MutableList<String> = ArrayList()
                        permissionsToExplain.add(RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION)
                        if (pb.explainReasonCallbackWithBeforeParam != null) {
                            // callback ExplainReasonCallbackWithBeforeParam prior to ExplainReasonCallback
                            pb.explainReasonCallbackWithBeforeParam!!.onExplainReason(
                                task.explainScope, permissionsToExplain, false
                            )
                        } else {
                            pb.explainReasonCallback!!.onExplainReason(
                                task.explainScope,
                                permissionsToExplain
                            )
                        }
                    } else if (pb.forwardToSettingsCallback != null && !shouldShowRationale) {
                        goesToOnPermissionCallback =
                            false // shouldn't because ForwardToSettingsCallback handles it
                        val permissionsToForward: MutableList<String> = ArrayList()
                        permissionsToForward.add(RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION)
                        pb.forwardToSettingsCallback!!.onForwardToSettings(
                            task.forwardScope,
                            permissionsToForward
                        )
                    }
                    // If showRequestReasonDialog or showForwardToSettingsDialog is not called. We should finish the task.
                    // There's case that ExplainReasonCallback or ForwardToSettingsCallback is called, but developer didn't invoke
                    // showRequestReasonDialog or showForwardToSettingsDialog in the callback.
                    // At this case and all other cases, task should be finished.
                    if (goesToOnPermissionCallback || !pb.showDialogCalled) {
                        task.finish()
                    }
                }
            }
        }
    }

    private fun onRequestSystemAlertWindowPermissionResult() {
        if (checkForGC()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(requireContext())) {
                    task.finish()
                } else if (pb.explainReasonCallback != null || pb.explainReasonCallbackWithBeforeParam != null) {
                    if (pb.explainReasonCallbackWithBeforeParam != null) {
                        // callback ExplainReasonCallbackWithBeforeParam prior to ExplainReasonCallback
                        pb.explainReasonCallbackWithBeforeParam!!.onExplainReason(
                            task.explainScope,
                            listOf(Manifest.permission.SYSTEM_ALERT_WINDOW),
                            false
                        )
                    } else {
                        pb.explainReasonCallback!!.onExplainReason(
                            task.explainScope, listOf(Manifest.permission.SYSTEM_ALERT_WINDOW)
                        )
                    }
                }
            } else {
                task.finish()
            }
        }
    }

    private fun onRequestWriteSettingsPermissionResult() {
        if (checkForGC()) {
            postForResult {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.System.canWrite(requireContext())) {
                        task.finish()
                    } else if (pb.explainReasonCallback != null || pb.explainReasonCallbackWithBeforeParam != null) {
                        if (pb.explainReasonCallbackWithBeforeParam != null) {
                            // callback ExplainReasonCallbackWithBeforeParam prior to ExplainReasonCallback
                            pb.explainReasonCallbackWithBeforeParam!!.onExplainReason(
                                task.explainScope, listOf(Manifest.permission.WRITE_SETTINGS), false
                            )
                        } else {
                            pb.explainReasonCallback!!.onExplainReason(
                                task.explainScope, listOf(Manifest.permission.WRITE_SETTINGS)
                            )
                        }
                    }
                } else {
                    task.finish()
                }
            }
        }
    }

    private fun onRequestManageExternalStoragePermissionResult() {
        if (checkForGC()) {
            postForResult {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        task.finish()
                    } else if (pb.explainReasonCallback != null || pb.explainReasonCallbackWithBeforeParam != null) {
                        if (pb.explainReasonCallbackWithBeforeParam != null) {
                            // callback ExplainReasonCallbackWithBeforeParam prior to ExplainReasonCallback
                            pb.explainReasonCallbackWithBeforeParam!!.onExplainReason(
                                task.explainScope,
                                listOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE),
                                false
                            )
                        } else {
                            pb.explainReasonCallback!!.onExplainReason(
                                task.explainScope,
                                listOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
                            )
                        }
                    }
                } else {
                    task.finish()
                }
            }
        }
    }

    private fun onRequestInstallPackagesPermissionResult() {
        if (checkForGC()) {
            postForResult {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (requireActivity().packageManager.canRequestPackageInstalls()) {
                        task.finish()
                    } else if (pb.explainReasonCallback != null || pb.explainReasonCallbackWithBeforeParam != null) {
                        if (pb.explainReasonCallbackWithBeforeParam != null) {
                            // callback ExplainReasonCallbackWithBeforeParam prior to ExplainReasonCallback
                            pb.explainReasonCallbackWithBeforeParam!!.onExplainReason(
                                task.explainScope,
                                listOf(Manifest.permission.REQUEST_INSTALL_PACKAGES),
                                false
                            )
                        } else {
                            pb.explainReasonCallback!!.onExplainReason(
                                task.explainScope,
                                listOf(Manifest.permission.REQUEST_INSTALL_PACKAGES)
                            )
                        }
                    }
                } else {
                    task.finish()
                }
            }
        }
    }

    private fun onRequestNotificationPermissionResult() {
        if (checkForGC()) {
            postForResult {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (DPermission.areNotificationsEnabled(requireContext())) {
                        task.finish()
                    } else if (pb.explainReasonCallback != null || pb.explainReasonCallbackWithBeforeParam != null) {
                        if (pb.explainReasonCallbackWithBeforeParam != null) {
                            // callback ExplainReasonCallbackWithBeforeParam prior to ExplainReasonCallback
                            pb.explainReasonCallbackWithBeforeParam!!.onExplainReason(
                                task.explainScope,
                                listOf(DPermission.permission.POST_NOTIFICATIONS),
                                false
                            )
                        } else {
                            pb.explainReasonCallback!!.onExplainReason(
                                task.explainScope,
                                listOf(DPermission.permission.POST_NOTIFICATIONS)
                            )
                        }
                    }
                } else {
                    task.finish()
                }
            }
        }
    }

    private fun onRequestBodySensorsBackgroundPermissionResult(granted: Boolean) {
        if (checkForGC()) {
            postForResult {
                if (granted) {
                    pb.grantedPermissions.add(RequestBodySensorsBackgroundPermission.BODY_SENSORS_BACKGROUND)
                    // Remove granted permissions from deniedPermissions and permanentDeniedPermissions set in PermissionBuilder.
                    pb.deniedPermissions.remove(RequestBodySensorsBackgroundPermission.BODY_SENSORS_BACKGROUND)
                    pb.permanentDeniedPermissions.remove(RequestBodySensorsBackgroundPermission.BODY_SENSORS_BACKGROUND)
                    task.finish()
                } else {
                    var goesToOnPermissionCallback = true // Indicate if we should finish the task
                    val shouldShowRationale =
                        shouldShowRequestPermissionRationale(RequestBodySensorsBackgroundPermission.BODY_SENSORS_BACKGROUND)
                    // If explainReasonCallback is not null and we should show rationale. Try the ExplainReasonCallback.
                    if ((pb.explainReasonCallback != null || pb.explainReasonCallbackWithBeforeParam != null) && shouldShowRationale) {
                        goesToOnPermissionCallback =
                            false // shouldn't because ExplainReasonCallback handles it
                        val permissionsToExplain: MutableList<String> = ArrayList()
                        permissionsToExplain.add(RequestBodySensorsBackgroundPermission.BODY_SENSORS_BACKGROUND)
                        if (pb.explainReasonCallbackWithBeforeParam != null) {
                            // callback ExplainReasonCallbackWithBeforeParam prior to ExplainReasonCallback
                            pb.explainReasonCallbackWithBeforeParam!!.onExplainReason(
                                task.explainScope, permissionsToExplain, false
                            )
                        } else {
                            pb.explainReasonCallback!!.onExplainReason(
                                task.explainScope,
                                permissionsToExplain
                            )
                        }
                    } else if (pb.forwardToSettingsCallback != null && !shouldShowRationale) {
                        goesToOnPermissionCallback =
                            false // shouldn't because ForwardToSettingsCallback handles it
                        val permissionsToForward: MutableList<String> = ArrayList()
                        permissionsToForward.add(RequestBodySensorsBackgroundPermission.BODY_SENSORS_BACKGROUND)
                        pb.forwardToSettingsCallback!!.onForwardToSettings(
                            task.forwardScope,
                            permissionsToForward
                        )
                    }

                    if (goesToOnPermissionCallback || !pb.showDialogCalled) {
                        task.finish()
                    }
                }
            }
        }
    }

    private fun checkForGC(): Boolean {
        if (!::pb.isInitialized || !::task.isInitialized || context == null) {
            Log.w(
                "DPermission",
                "PermissionBuilder and ChainTask should not be null at this time, so we can do nothing in this case."
            )
            return false
        }
        return true
    }

    private fun postForResult(callback: () -> Unit) {
        handler.post {
            callback()
        }
    }
}