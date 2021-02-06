package com.alibaba.testable.agent.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClassUtilTest {

    @Test
    void should_able_to_get_parameter_count() {
        assertEquals(0, ClassUtil.getParameterTypes("()V").size());
        assertEquals(1, ClassUtil.getParameterTypes("(Ljava/lang/String;)V").size());
        assertEquals(6, ClassUtil.getParameterTypes("(Ljava/lang/String;IDLjava/lang/String;ZLjava/net/URL;)V").size());
        assertEquals(10, ClassUtil.getParameterTypes("(ZLjava/lang/String;IJFDCSBZ)V").size());
        assertEquals(3, ClassUtil.getParameterTypes("(Ljava/lang/String;[I[Ljava/lang/String;)V").size());
    }

    @Test
    void should_able_to_extract_parameter() {
        assertEquals("", ClassUtil.extractParameters("()I"));
        assertEquals("Ljava/lang/String;", ClassUtil.extractParameters("(Ljava/lang/String;)I"));
    }

    @Test
    void should_able_to_get_return_type() {
        assertEquals("", ClassUtil.getReturnType("(Ljava/lang/String;)V"));
        assertEquals("java/lang/Integer", ClassUtil.getReturnType("(Ljava/lang/String;)I"));
        assertEquals("[I", ClassUtil.getReturnType("(Ljava/lang/String;)[I"));
        assertEquals("java/lang/String", ClassUtil.getReturnType("(Ljava/lang/String;)Ljava/lang/String;"));
        assertEquals("[Ljava/lang/String;", ClassUtil.getReturnType("(Ljava/lang/String;)[Ljava/lang/String;"));
    }

    @Test
    void should_able_to_convert_class_name() {
        assertEquals("Ljava/lang/String;", ClassUtil.toByteCodeClassName("java.lang.String"));
    }

    @Test
    void should_able_to_fit_companion_class_name() {
        assertEquals("com/intellij/rt/debugger/agent/CaptureAgent$ParamKeyProvider",
            ClassUtil.fitCompanionClassName("com/intellij/rt/debugger/agent/CaptureAgent$ParamKeyProvider"));
        assertEquals("com/alibaba/testable/demo/BlackBox",
            ClassUtil.fitCompanionClassName("com/alibaba/testable/demo/BlackBox"));
        assertEquals("com/alibaba/testable/demo/BlackBox$Companion",
            ClassUtil.fitCompanionClassName("com/alibaba/testable/demo/BlackBox$Companion$Companion"));
        assertEquals("com/alibaba/testable/demo/BlackBox",
            ClassUtil.fitCompanionClassName("com/alibaba/testable/demo/BlackBox$Companion"));
    }

}

