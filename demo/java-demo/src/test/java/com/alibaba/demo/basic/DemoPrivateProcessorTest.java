package com.alibaba.demo.basic;

import com.alibaba.testable.processor.annotation.EnablePrivateAccess;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 演示使用`@EnablePrivateAccess`注解访问私有成员
 * Demonstrate access private member via `@EnablePrivateAccess` annotation
 */
@EnablePrivateAccess(srcClass = DemoPrivateAccess.class)
class DemoPrivateProcessorTest {

    private DemoPrivateAccess demoPrivateAccess = new DemoPrivateAccess();

    @Test
    void should_access_private_method() {
        List<String> list = new ArrayList<String>() {{ add("a"); add("b"); add("c"); }};
        assertEquals("member", demoPrivateAccess.privateFunc());
        assertEquals("abc + hello + 1", demoPrivateAccess.privateFuncWithArgs(list, "hello", 1));
    }

    @Test
    void should_access_private_field() {
        demoPrivateAccess.count = 2;
        assertEquals(Integer.valueOf(2), demoPrivateAccess.count);
    }

    @Test
    void should_access_private_static_method() {
        assertEquals("static", DemoPrivateAccess.privateStaticFunc());
        assertEquals("hello + 1", DemoPrivateAccess.privateStaticFuncWithArgs("hello", 1));
    }

    @Test
    void should_access_private_static_field() {
        DemoPrivateAccess.staticCount = 2;
        assertEquals(Integer.valueOf(2), DemoPrivateAccess.staticCount);
    }

    @Test
    void should_update_final_field() {
        demoPrivateAccess.pi = 4.13;
        assertEquals(Double.valueOf(4.13), demoPrivateAccess.pi);
    }

    @Test
    void should_use_null_parameter() {
        demoPrivateAccess.pi = null;
        assertNull(demoPrivateAccess.pi);
        assertEquals("null + 1", DemoPrivateAccess.privateStaticFuncWithArgs(null, 1));
    }

}
