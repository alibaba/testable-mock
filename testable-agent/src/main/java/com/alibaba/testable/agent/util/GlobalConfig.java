package com.alibaba.testable.agent.util;

import com.alibaba.testable.core.model.LogLevel;
import com.alibaba.testable.core.model.MockScope;
import com.alibaba.testable.core.util.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.alibaba.testable.agent.constant.ConstPool.PROPERTY_USER_DIR;
import static com.alibaba.testable.core.util.PathUtil.createFolder;

/**
 * @author flin
 */
public class GlobalConfig {

    private static final String MUTE = "mute";
    private static final String DEBUG = "debug";
    private static final String VERBOSE = "verbose";
    private static final String DISABLE_LOG_FILE = "null";
    private static final String COMMA = ",";
    private static final String DOT = ".";

    private static final String TESTABLE_AGENT_LOG = "testable-agent.log";

    private static String logFile = null;
    private static String dumpPath = null;
    private static List<String> pkgPrefixes = new ArrayList<String>();
    private static MockScope defaultMockScope = MockScope.GLOBAL;
    private static boolean enhanceThreadLocal = false;
    private static boolean enhanceOmniConstructor = false;
    private static String innerMockClassName = "Mock";

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
        String fullPath = PathUtil.join(System.getProperty(PROPERTY_USER_DIR), path);
        if (createFolder(fullPath)) {
            dumpPath = fullPath;
        }
    }

    public static List<String> getPkgPrefixes() {
        return pkgPrefixes;
    }

    public static void setPkgPrefixes(String prefixes) {
        for (String p : prefixes.split(COMMA)) {
            pkgPrefixes.add(p.endsWith(DOT) ? p : p + DOT);
        }
    }

    public static MockScope getDefaultMockScope() {
        return defaultMockScope;
    }

    public static void setDefaultMockScope(MockScope scope) {
        defaultMockScope = scope;
    }

    public static void setupLogRootPath() {
        if (logFile == null) {
            String baseFolder = PathUtil.getFirstLevelFolder(System.getProperty(PROPERTY_USER_DIR),
                Object.class.getResource("/").getPath());
            if (!baseFolder.isEmpty()) {
                LogUtil.setGlobalLogPath(PathUtil.join(baseFolder, TESTABLE_AGENT_LOG));
            }
        } else if (!DISABLE_LOG_FILE.equals(logFile)) {
            LogUtil.setGlobalLogPath(PathUtil.join(System.getProperty(PROPERTY_USER_DIR), logFile));
        }
    }

    public static void setEnhanceThreadLocal(boolean enabled) {
        enhanceThreadLocal = enabled;
    }

    public static boolean isEnhanceThreadLocal() {
        return enhanceThreadLocal;
    }

    public static void setEnhanceOmniConstructor(boolean enabled) {
        enhanceOmniConstructor = enabled;
    }

    public static boolean isEnhanceOmniConstructor() {
        return enhanceOmniConstructor;
    }

    public static void setInnerMockClassName(String name) {
        innerMockClassName = name;
    }

    public static String getInnerMockClassName() {
        return innerMockClassName;
    }
}
