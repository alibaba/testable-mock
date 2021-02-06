package com.alibaba.testable.agent;

import com.alibaba.testable.agent.transformer.TestableClassTransformer;
import com.alibaba.testable.agent.util.GlobalConfig;

import java.lang.instrument.Instrumentation;

/**
 * Agent entry, dynamically modify the byte code of classes under testing
 * @author flin
 */
public class PreMain {

    private static final String AND = "&";
    private static final String LOG_LEVEL = "logLevel";
    private static final String DUMP_PATH = "dumpPath";
    private static final String PKG_PREFIX = "pkgPrefix";
    private static final String EQUAL = "=";

    public static void premain(String agentArgs, Instrumentation inst) {
        parseArgs(agentArgs);
        inst.addTransformer(new TestableClassTransformer());
    }

    private static void parseArgs(String args) {
        if (args == null) {
            return;
        }
        for (String a : args.split(AND)) {
            int i = a.indexOf(EQUAL);
            if (i > 0) {
                String k = a.substring(0, i);
                String v = a.substring(i + 1);
                if (k.equals(LOG_LEVEL)) {
                    GlobalConfig.setLogLevel(v);
                } else if (k.equals(DUMP_PATH)) {
                    GlobalConfig.setDumpPath(v);
                } else if (k.equals(PKG_PREFIX)) {
                    GlobalConfig.setPkgPrefix(v);
                }
            } else {
                GlobalConfig.setLogLevel(a);
            }
        }
    }

}
