package com.dp_th.dpermission.callback;

import androidx.annotation.NonNull;

import com.dp_th.dpermission.request.ExplainScope;

import java.util.List;

public interface ExplainReasonCallbackWithBeforeParam {
    void onExplainReason(@NonNull ExplainScope scope, @NonNull List<String> deniedList, boolean beforeRequest);
}
