package com.dp_th.dpermission;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public class DPermission {

    public static PermissionMediator with(@NonNull FragmentActivity activity) {
        return new PermissionMediator(activity);
    }

    public static PermissionMediator with(@NonNull Fragment fragment) {
        return new PermissionMediator(fragment);
    }

    public static boolean isGranted(@NonNull Context context, @NonNull String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean areNotificationsEnabled(@NonNull Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    public static final class permission {
        public static final String POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS";
    }
}
