package com.dp_th.dpermission.callback;

import androidx.annotation.NonNull;

import com.dp_th.dpermission.request.ReasonToAsk;

import java.util.List;

public interface CallbackReasonBeforeParam {
    void onExplainReason(@NonNull ReasonToAsk scope, @NonNull List<String> deniedList, boolean beforeRequest);
}
