package com.alibaba.demo.basic.model.omni;

public enum PodStatus {

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
    TERMINATED("terminated");

    PodStatus(String status) {}

}
