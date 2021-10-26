package com.alibaba.testable.agent.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassUtilTest {

    @Test
    void should_convert_class_name() {
        assertEquals("Ljava/lang/String;", ClassUtil.toByteCodeClassName("java.lang.String"));
    }

    @Test
    void should_fit_companion_class_name() {
        assertEquals("com/intellij/rt/debugger/agent/CaptureAgent$ParamKeyProvider",
            ClassUtil.fitCompanionClassName("com/intellij/rt/debugger/agent/CaptureAgent$ParamKeyProvider"));
        assertEquals("com/alibaba/testable/demo/BlackBox",
            ClassUtil.fitCompanionClassName("com/alibaba/testable/demo/BlackBox"));
        assertEquals("com/alibaba/testable/demo/BlackBox$Companion",
            ClassUtil.fitCompanionClassName("com/alibaba/testable/demo/BlackBox$Companion$Companion"));
        assertEquals("com/alibaba/testable/demo/BlackBox",
            ClassUtil.fitCompanionClassName("com/alibaba/testable/demo/BlackBox$Companion"));
    }

    @Test
    void should_get_outer_class_name() {
        assertEquals("com/alibaba/demo/basic/DemoMockTest",
            ClassUtil.toOuterClassName("com/alibaba/demo/basic/DemoMockTest$Inner"));
        assertEquals("com/alibaba/demo/basic/DemoMockTest",
            ClassUtil.toOuterClassName("com/alibaba/demo/basic/DemoMockTest"));
    }

}

