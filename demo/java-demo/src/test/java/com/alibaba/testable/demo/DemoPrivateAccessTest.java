package com.alibaba.testable.demo;

import com.alibaba.testable.core.accessor.PrivateAccessor;
import com.alibaba.testable.processor.annotation.EnablePrivateAccess;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 演示私有成员访问功能
 * Demonstrate private member access functionality
 */
@EnablePrivateAccess
class DemoPrivateAccessTest {

    private DemoPrivateAccess demoPrivateAccess = new DemoPrivateAccess();

    @Test
    void should_able_to_access_private_method() {
        assertEquals("hello - 1", demoPrivateAccess.privateFunc("hello", 1));
        assertEquals("hello - 1", PrivateAccessor.invoke(demoPrivateAccess, "privateFunc", "hello", 1));
    }

    @Test
    void should_able_to_access_private_field() {
        demoPrivateAccess.count = 2;
        assertEquals(new Integer(2), demoPrivateAccess.count);

        PrivateAccessor.set(demoPrivateAccess, "count", 3);
        assertEquals(new Integer(3), PrivateAccessor.get(demoPrivateAccess, "count"));
    }

    @Test
    void should_able_to_access_private_static_method() {
        assertEquals("hello + 1", DemoPrivateAccess.privateStaticFunc("hello", 1));
        assertEquals("hello + 1", PrivateAccessor.invokeStatic(DemoPrivateAccess.class, "privateStaticFunc", "hello", 1));
    }

    @Test
    void should_able_to_access_private_static_field() {
        DemoPrivateAccess.staticCount = 2;
        assertEquals(new Integer(2), DemoPrivateAccess.staticCount);

        PrivateAccessor.setStatic(DemoPrivateAccess.class, "staticCount", 3);
        assertEquals(new Integer(3), PrivateAccessor.getStatic(DemoPrivateAccess.class, "staticCount"));
    }

    @Test
    void should_able_to_update_final_field() {
        demoPrivateAccess.pi = 4.13;
        assertEquals(4.13, demoPrivateAccess.pi);
    }

}
