package com.alibaba.demo.basic.model.omni

enum class PodStatus(status: String) {
    /**
     * waiting
     */
    WAITING("waiting"),

    /**
     * running
     */
    RUNNING("running"),

    /**
     * terminated
     */
    TERMINATED("terminated")
}
