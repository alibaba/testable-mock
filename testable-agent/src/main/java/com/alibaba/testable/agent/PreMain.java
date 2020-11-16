package com.alibaba.testable.agent;

import com.alibaba.testable.agent.transformer.TestableClassTransformer;
import com.alibaba.testable.agent.util.LogUtil;

import java.lang.instrument.Instrumentation;

/**
 * Agent entry, dynamically modify the byte code of classes under testing
 * @author flin
 */
public class PreMain {

    private static final String AND = "&";
    private static final String DEBUG = "debug";

    public static void premain(String agentArgs, Instrumentation inst) {
        parseArgs(agentArgs);
        inst.addTransformer(new TestableClassTransformer());
    }

    private static void parseArgs(String args) {
        if (args == null) {
            return;
        }
        for (String a : args.split(AND)) {
            if (a.equals(DEBUG)) {
                LogUtil.enableDebugLog();
            }
        }
    }

}
