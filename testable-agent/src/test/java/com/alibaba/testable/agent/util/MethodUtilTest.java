package com.alibaba.testable.agent.util;

import com.alibaba.testable.core.accessor.PrivateAccessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MethodUtilTest {

    @Test
    void should_get_parameter_count() {
        assertEquals(0, MethodUtil.getParameterTypes("()V").size());
        assertEquals(1, MethodUtil.getParameterTypes("(Ljava/lang/String;)V").size());
        assertEquals(6, MethodUtil.getParameterTypes("(Ljava/lang/String;IDLjava/lang/String;ZLjava/net/URL;)V").size());
        assertEquals(10, MethodUtil.getParameterTypes("(ZLjava/lang/String;IJFDCSBZ)V").size());
        assertEquals(3, MethodUtil.getParameterTypes("(Ljava/lang/String;[I[Ljava/lang/String;)V").size());
    }

    @Test
    void should_extract_parameter() {
        assertEquals("", MethodUtil.extractParameters("()I"));
        assertEquals("Ljava/lang/String;", MethodUtil.extractParameters("(Ljava/lang/String;)I"));
    }

    @Test
    void should_get_return_type() {
        assertEquals("V", MethodUtil.getReturnType("(Ljava/lang/String;)V"));
        assertEquals("I", MethodUtil.getReturnType("(Ljava/lang/String;)I"));
        assertEquals("[I", MethodUtil.getReturnType("(Ljava/lang/String;)[I"));
        assertEquals("Ljava/lang/String;", MethodUtil.getReturnType("(Ljava/lang/String;)Ljava/lang/String;"));
        assertEquals("[Ljava/lang/String;", MethodUtil.getReturnType("(Ljava/lang/String;)[Ljava/lang/String;"));
    }

    @Test
    void should_get_first_parameter() {
        assertEquals("Ljava/lang/String;", MethodUtil.getFirstParameter("(Ljava/lang/String;Ljava/lang/Object;I)V"));
        assertEquals("Ljava/lang/String;", MethodUtil.getFirstParameter("(Ljava/lang/String;)V"));
        assertEquals("", MethodUtil.getFirstParameter("()V"));
    }

    @Test
    void should_convert_bytecode_parameters() {
        assertEquals("", PrivateAccessor.invokeStatic(MethodUtil.class, "toJavaParameterDesc", ""));
        assertEquals("void", PrivateAccessor.invokeStatic(MethodUtil.class, "toJavaParameterDesc", "V"));
        assertEquals("int, long", PrivateAccessor.invokeStatic(MethodUtil.class, "toJavaParameterDesc", "IJ"));
        assertEquals("int[], long[]", PrivateAccessor.invokeStatic(MethodUtil.class, "toJavaParameterDesc", "[I[J"));
        assertEquals("int, java.lang.String", PrivateAccessor.invokeStatic(MethodUtil.class, "toJavaParameterDesc", "ILjava/lang/String;"));
        assertEquals("java.lang.String, int, long[]", PrivateAccessor.invokeStatic(MethodUtil.class, "toJavaParameterDesc", "Ljava/lang/String;I[J"));
    }

}

