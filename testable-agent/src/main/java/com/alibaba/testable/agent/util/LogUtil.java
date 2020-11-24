package com.alibaba.testable.agent.util;

/**
 * @author flin
 */
public class LogUtil {

    private static final int LEVEL_ERROR = 0;
    private static final int LEVEL_WARN = 1;
    private static final int LEVEL_DIAGNOSE = 2;
    private static final ThreadLocal<Integer> LEVEL = new ThreadLocal<Integer>();

    public static boolean globalDebugEnable = false;

    public static void debug(String msg, Object... args) {
        if (LEVEL.get() >= LEVEL_DIAGNOSE) {
            System.out.println(String.format("[DIAGNOSE] " + msg, args));
        }
    }

    public static void enableDebugLog() {
        LEVEL.remove();
        LEVEL.set(LEVEL_DIAGNOSE);
    }

    public static void disableDebugLog() {
        LEVEL.remove();
        LEVEL.set(LEVEL_ERROR);
    }

    public static void resetDebugLog() {
        LEVEL.set(globalDebugEnable ? LEVEL_DIAGNOSE : LEVEL_WARN);
    }

}
