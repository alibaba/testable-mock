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
    private static final String DISABLE_LOG_FILE = "null";

    private static final String TESTABLE_AGENT_LOG = "testable-agent.log";

    private static String logFile = null;
    private static String dumpPath = null;
    private static String pkgPrefix = null;
    private static MockScope defaultMockScope = MockScope.GLOBAL;

    public static void setLogLevel(String level) {
        if (level.equals(MUTE)) {
            LogUtil.setDefaultLevel(LogLevel.DISABLE);
        } else if (level.equals(DEBUG)) {
            LogUtil.setDefaultLevel(LogLevel.ENABLE);
        } else if (level.equals(VERBOSE)) {
            LogUtil.setDefaultLevel(LogLevel.VERBOSE);
        }
    }

    public static void setLogFile(String path) {
        logFile = path;
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
        if (logFile == null) {
            String baseFolder = PathUtil.getFirstLevelFolder(System.getProperty(USER_DIR),
                Object.class.getResource("/").getPath());
            if (!baseFolder.isEmpty()) {
                LogUtil.setGlobalLogPath(PathUtil.join(baseFolder, TESTABLE_AGENT_LOG));
            }
        } else if (!DISABLE_LOG_FILE.equals(logFile)) {
            LogUtil.setGlobalLogPath(PathUtil.join(System.getProperty(USER_DIR), logFile));
        }
    }
}
