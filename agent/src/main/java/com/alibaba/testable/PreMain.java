package com.alibaba.testable;

import java.lang.instrument.Instrumentation;

public class PreMain {

    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new TestableFileTransformer());
    }

}

