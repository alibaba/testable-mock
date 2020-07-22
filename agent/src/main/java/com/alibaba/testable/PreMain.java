package com.alibaba.testable;

import com.alibaba.testable.transformer.TestableClassTransformer;

import java.lang.instrument.Instrumentation;

/**
 * Agent entry, dynamically modify the byte code of classes under testing
 * @author flin
 */
public class PreMain {

    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new TestableClassTransformer());
    }

}
