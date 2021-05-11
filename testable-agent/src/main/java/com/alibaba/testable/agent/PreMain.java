package com.alibaba.testable.agent;

import com.alibaba.testable.agent.config.ArgumentParser;
import com.alibaba.testable.agent.config.PropertiesParser;
import com.alibaba.testable.agent.transformer.TestableClassTransformer;
import com.alibaba.testable.agent.util.GlobalConfig;
import com.alibaba.testable.core.util.LogUtil;
import com.alibaba.ttl.threadpool.agent.TtlAgent;

import java.lang.instrument.Instrumentation;

import static com.alibaba.testable.agent.constant.ConstPool.PROPERTY_USER_DIR;

/**
 * Agent entry, dynamically modify the byte code of classes under testing
 * @author flin
 */
public class PreMain {

    public static void premain(String agentArgs, Instrumentation inst) {
        ArgumentParser.parseArgs(agentArgs);
        PropertiesParser.parseFile(ArgumentParser.configFilePath);
        GlobalConfig.setupLogRootPath();
        LogUtil.info("TestableMock start at %s", System.getProperty(PROPERTY_USER_DIR));
        if (GlobalConfig.enhanceThreadLocal) {
            // add transmittable thread local transformer
            TtlAgent.premain(agentArgs, inst);
        }
        // add testable mock transformer
        inst.addTransformer(new TestableClassTransformer());
        cleanup();
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
