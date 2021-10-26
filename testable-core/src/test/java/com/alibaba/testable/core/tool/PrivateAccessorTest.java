package com.alibaba.testable.core.tool;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class PrivateAccessorTest {

    static class A {}
    static class AB extends A {}
    static class AC extends A {}
    static class ABC extends AB {}
    static class B {}

    @Test
    void should_get_common_type() throws Exception {
        Method getCommonClassOf = PrivateAccessor.class.getDeclaredMethod("getCommonClassOf", Class.class, Class.class);
        getCommonClassOf.setAccessible(true);
        assertEquals(A.class, getCommonClassOf.invoke(null, A.class, A.class));
        assertEquals(A.class, getCommonClassOf.invoke(null, A.class, AB.class));
        assertEquals(A.class, getCommonClassOf.invoke(null, ABC.class, A.class));
        assertEquals(Object.class, getCommonClassOf.invoke(null, B.class, A.class));
        assertEquals(A.class, getCommonClassOf.invoke(null, ABC.class, AC.class));
    }

}
