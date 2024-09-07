package com.dp_th.dpermission.callback;

import androidx.annotation.NonNull;

import com.dp_th.dpermission.request.ReasonToAsk;

import java.util.List;

public interface RequestReasonCallback {
    void onExplainReason(@NonNull ReasonToAsk reason, @NonNull List<String> deniedList);
}
