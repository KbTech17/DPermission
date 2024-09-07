package com.dp_th.dpermission.request

class RequestChain {
    private var headTask: BaseTask? = null
    private var tailTask: BaseTask? = null
    internal fun addTaskToChain(task: BaseTask) {
        if (headTask == null) {
            headTask = task
        }
        tailTask?.next = task
        tailTask = task
    }

    internal fun runTask() {
        headTask?.request()
    }
}