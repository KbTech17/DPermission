package com.dp_th.dpermission.request;

import java.util.List;

public interface ChainTask {

    ExplainScope getExplainScope();

    ForwardScope getForwardScope();

    void request();

    void requestAgain(List<String> permissions);

    void finish();
}