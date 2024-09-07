package com.dp_th.dpermission.request;

import java.util.List;

public interface ReasonTask {

    ReasonToAsk getExplainScope();

    ManualScope getForwardScope();

    void request();

    void requestAgain(List<String> permissions);

    void finish();
}