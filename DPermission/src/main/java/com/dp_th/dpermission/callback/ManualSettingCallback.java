package com.dp_th.dpermission.callback;

import androidx.annotation.NonNull;

import com.dp_th.dpermission.request.ManualScope;

import java.util.List;

public interface ManualSettingCallback {
    void onManualSettings(@NonNull ManualScope manual, @NonNull List<String> deniedList);
}