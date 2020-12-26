package com.alibaba.testable.agent.util;

import com.alibaba.testable.core.util.LogUtil;

/**
 * @author flin
 */
public class GlobalConfig {

    private static final String MUTE = "mute";
    private static final String DEBUG = "debug";
    private static final String VERBOSE = "verbose";

    private static String dumpPath = null;

    public static boolean setLogLevel(String level) {
        if (level.equals(MUTE)) {
            LogUtil.setDefaultLevel(LogUtil.LogLevel.LEVEL_MUTE);
            return true;
        } else if (level.equals(DEBUG)) {
            LogUtil.setDefaultLevel(LogUtil.LogLevel.LEVEL_DIAGNOSE);
            return true;
        } else if (level.equals(VERBOSE)) {
            LogUtil.setDefaultLevel(LogUtil.LogLevel.LEVEL_VERBOSE);
            return true;
        }
        return false;
    }

    public static void setDumpPath(String path) {
        dumpPath = path;
    }

    public static String getDumpPath() {
        return dumpPath;
    }

}
