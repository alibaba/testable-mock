package com.alibaba.testable.agent.util;

/**
 * @author flin
 */
public class LogUtil {

    private static final int LEVEL_INFO = 0;
    private static final int LEVEL_DEBUG = 2;
    private static int level = LEVEL_INFO;

    public static void debug(String msg, Object... args) {
        if (level >= LEVEL_DEBUG) {
            System.err.println(String.format("[DEBUG] " + msg, args));
        }
    }

    public static void enableDebugLog() {
        level = LEVEL_DEBUG;
    }
}
