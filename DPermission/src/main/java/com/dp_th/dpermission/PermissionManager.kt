package com.dp_th.dpermission

import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.dp_th.dpermission.dialog.allSpecialPermissions
import com.dp_th.dpermission.request.PermissionBuilder
import com.dp_th.dpermission.request.BackgroundLocationRequest
import kotlin.collections.LinkedHashSet

class PermissionManager {

    private var activity: FragmentActivity? = null
    private var fragment: Fragment? = null

    constructor(activity: FragmentActivity) {
        this.activity = activity
    }

    constructor(fragment: Fragment) {
        this.fragment = fragment
    }

    fun permissions(permissions: List<String>): PermissionBuilder {
        val normalPermissionSet = LinkedHashSet<String>()
        val specialPermissionSet = LinkedHashSet<String>()
        val osVersion = Build.VERSION.SDK_INT
        val targetSdkVersion = if (activity != null) {
            activity!!.applicationInfo.targetSdkVersion
        } else {
            fragment!!.requireContext().applicationInfo.targetSdkVersion
        }
        for (permission in permissions) {
            if (permission in allSpecialPermissions) {
                specialPermissionSet.add(permission)
            } else {
                normalPermissionSet.add(permission)
            }
        }
        if (BackgroundLocationRequest.ACCESS_BACKGROUND_LOCATION in specialPermissionSet) {
            if (osVersion == Build.VERSION_CODES.Q ||
                (osVersion == Build.VERSION_CODES.R && targetSdkVersion < Build.VERSION_CODES.R)) {
                specialPermissionSet.remove(BackgroundLocationRequest.ACCESS_BACKGROUND_LOCATION)
                normalPermissionSet.add(BackgroundLocationRequest.ACCESS_BACKGROUND_LOCATION)
            }
        }
        if (DPermission.permission.POST_NOTIFICATIONS in specialPermissionSet) {
            if (osVersion >= Build.VERSION_CODES.TIRAMISU && targetSdkVersion >= Build.VERSION_CODES.TIRAMISU) {
                specialPermissionSet.remove(DPermission.permission.POST_NOTIFICATIONS)
                normalPermissionSet.add(DPermission.permission.POST_NOTIFICATIONS)
            }
        }
        if (DPermission.permission.SCHEDULE_EXACT_ALARM in specialPermissionSet) {
            if (osVersion >= Build.VERSION_CODES.TIRAMISU && targetSdkVersion >= Build.VERSION_CODES.TIRAMISU) {
                specialPermissionSet.remove(DPermission.permission.SCHEDULE_EXACT_ALARM)
                normalPermissionSet.add(DPermission.permission.SCHEDULE_EXACT_ALARM)
            }
        }
        return PermissionBuilder(activity, fragment, normalPermissionSet, specialPermissionSet)
    }

    fun permissions(vararg permissions: String): PermissionBuilder {
        return permissions(listOf(*permissions))
    }

}