package com.alibaba.testable.agent.config;

import com.alibaba.testable.agent.util.GlobalConfig;
import com.alibaba.testable.agent.util.PathUtil;
import com.alibaba.testable.core.model.MockScope;
import com.alibaba.testable.core.util.LogUtil;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import static com.alibaba.testable.agent.constant.ConstPool.PROPERTY_USER_DIR;

public class PropertiesParser {

    private static final String DEFAULT_CONFIG_FILE = "src/test/resources/testable.properties";
    private static final String DUMP_PATH = "dump.path";
    private static final String PKG_PREFIX_EXCLUDES = "enhance.pkgPrefix.excludes";
    private static final String PKG_PREFIX_INCLUDES = "enhance.pkgPrefix.includes";
    private static final String LOG_FILE = "log.file";
    private static final String LOG_LEVEL = "log.level";
    private static final String ENABLE_MOCK_INJECT = "mock.enhance.enable";
    private static final String INNER_MOCK_CLASS_NAME = "mock.innerClass.name";
    private static final String MOCK_PKG_MAPPING_PREFIX = "mock.package.mapping.";
    private static final String DEFAULT_MOCK_SCOPE = "mock.scope.default";
    private static final String ENABLE_MOCK_TARGET_CHECK = "mock.target.checking.enable";
    private static final String ENABLE_OMNI_INJECT = "omni.constructor.enhance.enable";
    private static final String ENABLE_THREAD_POOL = "thread.pool.enhance.enable";

    public static void parseFile(String configFilePath) {
        String path = (configFilePath == null) ? DEFAULT_CONFIG_FILE : configFilePath;
        String fullPath = PathUtil.isAbsolutePath(path) ? path :
            PathUtil.join(System.getProperty(PROPERTY_USER_DIR), path);
        Properties pps = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(fullPath));
            pps.load(in);
            LogUtil.verbose("Loaded configure file %s", fullPath);
        } catch (IOException e) {
            if (configFilePath == null) {
                LogUtil.verbose("No configure file found, skip.");
            } else {
                LogUtil.warn("No configure file found at %s", fullPath);
            }
            return;
        }
        parsePropertiesContent(pps);
    }

    private static void parsePropertiesContent(Properties pps) {
        Enumeration<?> en = pps.propertyNames();
        while(en.hasMoreElements()) {
            String k = (String)en.nextElement();
            String v = pps.getProperty(k);
            if (k.equals(DUMP_PATH)) {
                GlobalConfig.setDumpPath(v);
            } else if (k.equals(PKG_PREFIX_EXCLUDES)) {
                GlobalConfig.setPkgPrefixBlackList(v);
            } else if (k.equals(PKG_PREFIX_INCLUDES)) {
                GlobalConfig.setPkgPrefixWhiteList(v);
            } else if (k.equals(LOG_FILE)) {
                GlobalConfig.setLogFile(v);
            } else if (k.equals(LOG_LEVEL)) {
                GlobalConfig.setLogLevel(v);
            } else if (k.equals(INNER_MOCK_CLASS_NAME)) {
                GlobalConfig.innerMockClassName = v;
            } else if (k.startsWith(MOCK_PKG_MAPPING_PREFIX)) {
                GlobalConfig.addMockPackageMapping(k.substring(MOCK_PKG_MAPPING_PREFIX.length()), v);
            } else if (k.equals(DEFAULT_MOCK_SCOPE)) {
                GlobalConfig.defaultMockScope = MockScope.of(v);
            } else if (k.equals(ENABLE_OMNI_INJECT)) {
                GlobalConfig.enhanceOmniConstructor = Boolean.parseBoolean(v);
            } else if (k.equals(ENABLE_MOCK_INJECT)) {
                GlobalConfig.enhanceMock = Boolean.parseBoolean(v);
            } else if (k.equals(ENABLE_MOCK_TARGET_CHECK)) {
                GlobalConfig.checkMockTargetExistence = Boolean.parseBoolean(v);
            } else if (k.equals(ENABLE_THREAD_POOL)) {
                GlobalConfig.enhanceThreadLocal = Boolean.parseBoolean(v);
            }
        }
    }

}
