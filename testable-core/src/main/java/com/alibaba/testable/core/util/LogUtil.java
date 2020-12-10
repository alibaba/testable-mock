package com.alibaba.testable.core.util;

/**
 * @author flin
 */
public class LogUtil {

    public static final int LEVEL_ERROR = 0;
    public static final int LEVEL_WARN = 1;
    public static final int LEVEL_DIAGNOSE = 2;
    public static final int LEVEL_VERBOSE = 3;

    private static int defaultLogLevel = LEVEL_WARN;
    private static int level;

    public static void verbose(String msg, Object... args) {
        if (level >= LEVEL_VERBOSE) {
            System.out.println(String.format("[VERBOSE] " + msg, args));
        }
    }

    public static void diagnose(String msg, Object... args) {
        if (level >= LEVEL_DIAGNOSE) {
            System.out.println(String.format("[DIAGNOSE] " + msg, args));
        }
    }

    public static void warn(String msg, Object... args) {
        if (level >= LEVEL_WARN) {
            System.out.println(String.format("[WARN] " + msg, args));
        }
    }

    public static void enableDiagnose(boolean enable) {
        level = enable ? LEVEL_DIAGNOSE : LEVEL_ERROR;
    }

    public static void setDefaultLevel(int level) {
        defaultLogLevel = level;
        resetLogLevel();
    }

    public static void resetLogLevel() {
        level = defaultLogLevel;
    }

}
