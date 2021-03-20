package com.alibaba.demo.basic.model.omni

class Probe {
    var exec: ExecAction? = null
    var failureThreshold: Int? = null
    var initialDelaySeconds: Int? = null
    var periodSeconds: Int? = null
    var successThreshold: Int? = null
    var timeoutSeconds: Int? = null
}
