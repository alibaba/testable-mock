package com.alibaba.testable.agent.util;

import com.alibaba.testable.core.model.LogLevel;
import com.alibaba.testable.core.model.MockScope;
import com.alibaba.testable.core.util.LogUtil;

import java.io.File;

/**
 * @author flin
 */
public class GlobalConfig {

    private static final String MUTE = "mute";
    private static final String DEBUG = "debug";
    private static final String VERBOSE = "verbose";
    private static final String USER_DIR = "user.dir";

    private static String dumpPath = null;
    private static String pkgPrefix = null;
    private static MockScope defaultMockScope = MockScope.GLOBAL;

    public static boolean setLogLevel(String level) {
        if (level.equals(MUTE)) {
            LogUtil.setDefaultLevel(LogLevel.DISABLE);
            return true;
        } else if (level.equals(DEBUG)) {
            LogUtil.setDefaultLevel(LogLevel.ENABLE);
            return true;
        } else if (level.equals(VERBOSE)) {
            LogUtil.setDefaultLevel(LogLevel.VERBOSE);
            return true;
        }
        return false;
    }

    public static String getDumpPath() {
        return (dumpPath == null || dumpPath.isEmpty() || !new File(dumpPath).isDirectory()) ? null : dumpPath;
    }

    public static void setDumpPath(String path) {
        dumpPath = path;
    }

    public static String getPkgPrefix() {
        return pkgPrefix;
    }

    public static void setPkgPrefix(String prefix) {
        pkgPrefix = prefix;
    }

    public static MockScope getDefaultMockScope() {
        return defaultMockScope;
    }

    public static void setDefaultMockScope(MockScope scope) {
        defaultMockScope = scope;
    }

    public static void setupLogRootPath() {
        LogUtil.setGlobalLogPath(
            PathUtil.getFirstLevelFolder(System.getProperty(USER_DIR), Object.class.getResource("/").getPath()));
    }
}
