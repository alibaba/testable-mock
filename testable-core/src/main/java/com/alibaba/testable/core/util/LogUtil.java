package com.alibaba.testable.core.util;

import com.alibaba.testable.core.model.LogLevel;

/**
 * @author flin
 */
public class LogUtil {

    private static LogLevel defaultLogLevel = LogLevel.DEFAULT;
    private static LogLevel currentLogLevel = LogLevel.DEFAULT;

    public static void verbose(String msg, Object... args) {
        if (isVerboseEnabled()) {
            System.out.println(String.format("[VERBOSE] " + msg, args));
        }
    }

    public static void diagnose(String msg, Object... args) {
        if (currentLogLevel.level >= LogLevel.ENABLE.level) {
            System.out.println(String.format("[DIAGNOSE] " + msg, args));
        }
    }

    public static void warn(String msg, Object... args) {
        if (currentLogLevel.level >= LogLevel.DEFAULT.level) {
            System.err.println(String.format("[WARN] " + msg, args));
        }
    }

    public static void error(String msg, Object... args) {
        System.err.println(String.format("[ERROR] " + msg, args));
    }

    /**
     * a pre-check method for reduce verbose parameter calculation
     */
    public static boolean isVerboseEnabled() {
        return currentLogLevel.level >= LogLevel.VERBOSE.level;
    }

    public static void setLevel(LogLevel level) {
        currentLogLevel = level;
    }

    public static void setDefaultLevel(LogLevel level) {
        defaultLogLevel = level;
        resetLogLevel();
    }

    public static void resetLogLevel() {
        currentLogLevel = defaultLogLevel;
    }

}
