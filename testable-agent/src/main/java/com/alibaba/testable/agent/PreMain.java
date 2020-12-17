package com.alibaba.testable.agent;

import com.alibaba.testable.agent.transformer.TestableClassTransformer;
import com.alibaba.testable.core.util.LogUtil;

import java.lang.instrument.Instrumentation;

/**
 * Agent entry, dynamically modify the byte code of classes under testing
 * @author flin
 */
public class PreMain {

    private static final String AND = "&";
    private static final String MUTE = "mute";
    private static final String DEBUG = "debug";
    private static final String VERBOSE = "verbose";
    private static final String LOG_LEVEL = "logLevel";
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
                    setLogLevel(v);
                }
            } else {
                setLogLevel(a);
            }
        }
    }

    private static boolean setLogLevel(String level) {
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

}
