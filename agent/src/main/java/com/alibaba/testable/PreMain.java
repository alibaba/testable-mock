package com.alibaba.testable;

import com.alibaba.testable.transformer.TestableFileTransformer;

import java.lang.instrument.Instrumentation;

/**
 * @author flin
 */
public class PreMain {

    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new TestableFileTransformer());
    }

}
