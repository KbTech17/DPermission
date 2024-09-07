package com.dp_th.dpermission.callback;

import androidx.annotation.NonNull;

import com.dp_th.dpermission.request.ForwardScope;

import java.util.List;

public interface ForwardToSettingsCallback {

    void onForwardToSettings(@NonNull ForwardScope scope, @NonNull List<String> deniedList);

}