package com.dp_th.dpermission.callback;

import androidx.annotation.NonNull;

import java.util.List;

public interface OnPermissionCallback {
    void onPermissionResult(boolean isGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList);
}
