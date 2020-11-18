package com.alibaba.testable.agent.util;

/**
 * @author flin
 */
public class LogUtil {

    private static final int LEVEL_ERROR = 0;
    private static final int LEVEL_WARN = 1;
    private static final int LEVEL_DEBUG = 2;
    private static final ThreadLocal<Integer> LEVEL = new ThreadLocal<Integer>();

    public static boolean globalDebugEnable = false;

    public static void debug(String msg, Object... args) {
        if (LEVEL.get() >= LEVEL_DEBUG) {
            System.err.println(String.format("[DEBUG] " + msg, args));
        }
    }

    public static void enableDebugLog() {
        LEVEL.remove();
        LEVEL.set(LEVEL_DEBUG);
    }

    public static void disableDebugLog() {
        LEVEL.remove();
        LEVEL.set(LEVEL_ERROR);
    }

    public static void resetDebugLog() {
        LEVEL.set(globalDebugEnable ? LEVEL_DEBUG : LEVEL_WARN);
    }

}
