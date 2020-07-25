package com.alibaba.testable.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClassUtilTest {

    @Test
    void should_able_to_get_parameter_count() {
        assertEquals(1, ClassUtil.getParameterCount("(Ljava/lang/String;)V"));
        assertEquals(6, ClassUtil.getParameterCount("(Ljava/lang/String;IDLjava/lang/String;ZLjava/net/URL;)V"));
        assertEquals(10, ClassUtil.getParameterCount("(ZLjava/lang/String;IJFDCSBZ)V"));
        assertEquals(3, ClassUtil.getParameterCount("(Ljava/lang/String;[I[Ljava/lang/String;)V"));
    }

    @Test
    void should_able_to_get_return_type() {
        assertEquals("", ClassUtil.getReturnType("(Ljava/lang/String;)V"));
        assertEquals("java/lang/Integer", ClassUtil.getReturnType("(Ljava/lang/String;)I"));
        assertEquals("[I", ClassUtil.getReturnType("(Ljava/lang/String;)[I"));
        assertEquals("java/lang/String", ClassUtil.getReturnType("(Ljava/lang/String;)Ljava/lang/String;"));
        assertEquals("[Ljava/lang/String;", ClassUtil.getReturnType("(Ljava/lang/String;)[Ljava/lang/String;"));
    }

}
