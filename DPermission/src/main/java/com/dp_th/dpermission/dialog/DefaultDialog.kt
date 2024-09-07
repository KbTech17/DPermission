package com.dp_th.dpermission.dialog

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.dp_th.dpermission.R
import com.dp_th.dpermission.databinding.LayoutDialogBinding
import com.dp_th.dpermission.databinding.ItemPermissionBinding

class DefaultDialog(context: Context,
    private val permissions: List<String>,
    private val message: String,
    private val positiveText: String,
    private val negativeText: String?,
    private val lightColor: Int,
    private val darkColor: Int
) : RationaleDialog(context, R.style.DPermissionDefaultDialog) {

    private lateinit var binding: LayoutDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupText()
        buildPermissionsLayout()
        setupWindow()
    }

    override fun getPositiveButton(): View {
        return binding.positiveBtn
    }

    override fun getNegativeButton(): View? {
        return negativeText?.let {
            return binding.negativeBtn
        }
    }

    override fun getPermissionsToRequest(): List<String> {
        return permissions
    }

    internal fun isPermissionLayoutEmpty(): Boolean {
        return binding.permissionsLayout.childCount == 0
    }

    private fun setupText() {
        binding.messageText.text = message
        binding.positiveBtn.text = positiveText
        if (negativeText != null) {
            binding.negativeLayout.visibility = View.VISIBLE
            binding.negativeBtn.text = negativeText
        } else {
            binding.negativeLayout.visibility = View.GONE
        }
        if (isDarkTheme()) {
            if (darkColor != -1) {
                binding.positiveBtn.setTextColor(darkColor)
                binding.negativeBtn.setTextColor(darkColor)
            }
        } else {
            if (lightColor != -1) {
                binding.positiveBtn.setTextColor(lightColor)
                binding.negativeBtn.setTextColor(lightColor)
            }
        }
    }

    private fun buildPermissionsLayout() {
        val tempSet = HashSet<String>()
        val currentVersion = Build.VERSION.SDK_INT
        for (permission in permissions) {
            val permissionGroup = when {
                currentVersion < Build.VERSION_CODES.Q -> {
                    try {
                        val permissionInfo = context.packageManager.getPermissionInfo(permission, 0)
                        permissionInfo.group
                    } catch (e: PackageManager.NameNotFoundException) {
                        e.printStackTrace()
                        null
                    }
                }
                currentVersion == Build.VERSION_CODES.Q -> permissionMapOnQ[permission]
                currentVersion == Build.VERSION_CODES.R -> permissionMapOnR[permission]
                currentVersion == Build.VERSION_CODES.S -> permissionMapOnS[permission]
                currentVersion == Build.VERSION_CODES.TIRAMISU -> permissionMapOnT[permission]
                else -> {
                    permissionMapOnT[permission]
                }
            }
            if ((permission in allSpecialPermissions && !tempSet.contains(permission))
                || (permissionGroup != null && !tempSet.contains(permissionGroup))) {
                val itemBinding = ItemPermissionBinding.inflate(layoutInflater, binding.permissionsLayout, false)
                when {
                    permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION -> {
                        itemBinding.permissionText.text = context.getString(R.string.background_location)
                        itemBinding.permissionIcon.setImageResource(context.packageManager.getPermissionGroupInfo(permissionGroup!!, 0).icon)
                    }
                    permission == Manifest.permission.SYSTEM_ALERT_WINDOW -> {
                        itemBinding.permissionText.text = context.getString(R.string.system_alert_window)
                        itemBinding.permissionIcon.setImageResource(R.drawable.ic_alert)
                    }
                    permission == Manifest.permission.WRITE_SETTINGS -> {
                        itemBinding.permissionText.text = context.getString(R.string.write_settings)
                        itemBinding.permissionIcon.setImageResource(R.drawable.ic_setting)
                    }
                    permission == Manifest.permission.MANAGE_EXTERNAL_STORAGE -> {
                        itemBinding.permissionText.text = context.getString(R.string.manage_external_storage)
                        itemBinding.permissionIcon.setImageResource(context.packageManager.getPermissionGroupInfo(permissionGroup!!, 0).icon)
                    }
                    permission == Manifest.permission.REQUEST_INSTALL_PACKAGES -> {
                        itemBinding.permissionText.text = context.getString(R.string.request_install_packages)
                        itemBinding.permissionIcon.setImageResource(R.drawable.ic_install)
                    }
                    permission == Manifest.permission.POST_NOTIFICATIONS
                            && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> {
                        // When OS version is lower than Android 13, there isn't a notification icon or labelRes for us to get.
                        // So we need to handle it as special permission's way.
                        itemBinding.permissionText.text = context.getString(R.string.post_notification)
                        itemBinding.permissionIcon.setImageResource(R.drawable.ic_notification)
                    }
                    permission == Manifest.permission.BODY_SENSORS_BACKGROUND -> {
                        itemBinding.permissionText.text = context.getString(R.string.sensor_background)
                        itemBinding.permissionIcon.setImageResource(context.packageManager.getPermissionGroupInfo(permissionGroup!!, 0).icon)
                    }
                    else -> {
                        itemBinding.permissionText.text = context.getString(context.packageManager.getPermissionGroupInfo(permissionGroup!!, 0).labelRes)
                        itemBinding.permissionIcon.setImageResource(context.packageManager.getPermissionGroupInfo(permissionGroup, 0).icon)
                    }
                }
                if (isDarkTheme()) {
                    if (darkColor != -1) {
                        itemBinding.permissionIcon.setColorFilter(darkColor, PorterDuff.Mode.SRC_ATOP)
                    }
                } else {
                    if (lightColor != -1) {
                        itemBinding.permissionIcon.setColorFilter(lightColor, PorterDuff.Mode.SRC_ATOP)
                    }
                }
                binding.permissionsLayout.addView(itemBinding.root)
                tempSet.add(permissionGroup ?: permission)
            }
        }
    }

    private fun setupWindow() {
        val width = context.resources.displayMetrics.widthPixels
        val height = context.resources.displayMetrics.heightPixels
        if (width < height) {
            // now we are in portrait
            window?.let {
                val param = it.attributes
                it.setGravity(Gravity.CENTER)
                param.width = (width * 0.86).toInt()
                it.attributes = param
            }
        } else {
            // now we are in landscape
            window?.let {
                val param = it.attributes
                it.setGravity(Gravity.CENTER)
                param.width = (width * 0.6).toInt()
                it.attributes = param
            }
        }
    }

    private fun isDarkTheme(): Boolean {
        val flag = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return flag == Configuration.UI_MODE_NIGHT_YES
    }

}