package com.alibaba.testable.core.model;

/**
 * @author flin
 */
public enum LogLevel {
    /**
     * Mute
     */
    DISABLE(0),
    /**
     * Warn only
     */
    DEFAULT(1),
    /**
     * Show diagnose messages
     */
    ENABLE(2),
    /**
     * Show detail progress logs
     */
    VERBOSE(3);

    public int level;
    LogLevel(int level) {
        this.level = level;
    }
}
