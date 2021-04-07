package com.alibaba.testable.agent;

import com.alibaba.testable.agent.config.PropertiesParser;
import com.alibaba.testable.agent.transformer.TestableClassTransformer;
import com.alibaba.testable.agent.util.GlobalConfig;
import com.alibaba.testable.core.model.MockScope;
import com.alibaba.testable.core.util.LogUtil;
import com.alibaba.ttl.threadpool.agent.TtlAgent;

import java.lang.instrument.Instrumentation;

/**
 * Agent entry, dynamically modify the byte code of classes under testing
 * @author flin
 */
public class PreMain {

    private static final String AND = "&";
    private static final String USE_THREAD_POOL = "useThreadPool";
    private static final String LOG_LEVEL = "logLevel";
    private static final String LOG_FILE = "logFile";
    private static final String DUMP_PATH = "dumpPath";
    private static final String PKG_PREFIX = "pkgPrefix";
    private static final String MOCK_SCOPE = "mockScope";
    private static final String CONFIG_FILE = "configFile";
    private static final String EQUAL = "=";
    private static String configFilePath = null;

    public static void premain(String agentArgs, Instrumentation inst) {
        parseArgs(agentArgs);
        new PropertiesParser().parseFile(configFilePath);
        GlobalConfig.setupLogRootPath();
        if (GlobalConfig.isEnhanceThreadLocal()) {
            // add transmittable thread local transformer
            TtlAgent.premain(agentArgs, inst);
        }
        // add testable mock transformer
        inst.addTransformer(new TestableClassTransformer());
        cleanup();
    }

    private static void parseArgs(String args) {
        if (args == null) {
            return;
        }
        for (String a : args.split(AND)) {
            int i = a.indexOf(EQUAL);
            if (i > 0) {
                // parameter with key = value
                String k = a.substring(0, i);
                String v = a.substring(i + 1);
                if (k.equals(LOG_LEVEL)) {
                    GlobalConfig.setLogLevel(v);
                } else if (k.equals(LOG_FILE)) {
                    GlobalConfig.setLogFile(v);
                } else if (k.equals(DUMP_PATH)) {
                    GlobalConfig.setDumpPath(v);
                } else if (k.equals(PKG_PREFIX)) {
                    GlobalConfig.setPkgPrefix(v);
                } else if (k.equals(MOCK_SCOPE)) {
                    GlobalConfig.setDefaultMockScope(MockScope.of(v));
                } else if (k.equals(CONFIG_FILE)) {
                    configFilePath = v;
                }
            } else {
                // parameter with single value
                if (a.equals(USE_THREAD_POOL)) {
                    GlobalConfig.setEnhanceThreadLocal(true);
                }
            }
        }
    }

    private static void cleanup() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LogUtil.cleanup();
            }
        });
    }

}
