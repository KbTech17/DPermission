package com.dp_th.dpermission.callback;

import androidx.annotation.NonNull;

import java.util.List;

public interface RequestCallback {
    void onResult(boolean allGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList);
}
