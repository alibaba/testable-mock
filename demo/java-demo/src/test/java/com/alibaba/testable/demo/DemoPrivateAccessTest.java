package com.alibaba.testable.demo;

import com.alibaba.testable.core.accessor.PrivateAccessor;
import com.alibaba.testable.processor.annotation.EnablePrivateAccess;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * 演示私有成员访问功能
 * Demonstrate private member access functionality
 */
@EnablePrivateAccess
public class DemoPrivateAccessTest {

    private DemoPrivateAccess demoPrivateAccess = new DemoPrivateAccess();

    @Test
    public void should_able_to_access_private_method() {
        List<String> list = new ArrayList<String>() {{ add("a"); add("b"); add("c"); }};
        assertEquals("member", demoPrivateAccess.privateFunc());
        assertEquals("member", PrivateAccessor.invoke(demoPrivateAccess, "privateFunc"));
        assertEquals("abc + hello + 1", demoPrivateAccess.privateFuncWithArgs(list, "hello", 1));
        assertEquals("abc + hello + 1", PrivateAccessor.invoke(demoPrivateAccess, "privateFuncWithArgs", list, "hello", 1));
    }

    @Test
    public void should_able_to_access_private_field() {
        demoPrivateAccess.count = 2;
        assertEquals(Integer.valueOf(2), demoPrivateAccess.count);

        PrivateAccessor.set(demoPrivateAccess, "count", 3);
        assertEquals(Integer.valueOf(3), PrivateAccessor.get(demoPrivateAccess, "count"));
    }

    @Test
    public void should_able_to_access_private_static_method() {
        assertEquals("static", DemoPrivateAccess.privateStaticFunc());
        assertEquals("static", PrivateAccessor.invokeStatic(DemoPrivateAccess.class, "privateStaticFunc"));
        assertEquals("hello + 1", DemoPrivateAccess.privateStaticFuncWithArgs("hello", 1));
        assertEquals("hello + 1", PrivateAccessor.invokeStatic(DemoPrivateAccess.class, "privateStaticFuncWithArgs", "hello", 1));
    }

    @Test
    public void should_able_to_access_private_static_field() {
        DemoPrivateAccess.staticCount = 2;
        assertEquals(Integer.valueOf(2), DemoPrivateAccess.staticCount);

        PrivateAccessor.setStatic(DemoPrivateAccess.class, "staticCount", 3);
        assertEquals(Integer.valueOf(3), PrivateAccessor.getStatic(DemoPrivateAccess.class, "staticCount"));
    }

    @Test
    public void should_able_to_update_final_field() {
        demoPrivateAccess.pi = 4.13;
        assertEquals(Double.valueOf(4.13), demoPrivateAccess.pi);

        PrivateAccessor.set(demoPrivateAccess, "pi", 3.14);
        assertEquals(Double.valueOf(3.14), PrivateAccessor.get(demoPrivateAccess, "pi"));
    }

    @Test
    public void should_able_to_use_null_parameter() {
        demoPrivateAccess.pi = null;
        assertNull(demoPrivateAccess.pi);
        assertEquals("null + 1", DemoPrivateAccess.privateStaticFuncWithArgs(null, 1));

        PrivateAccessor.set(demoPrivateAccess, "pi", null);
        assertNull(PrivateAccessor.get(demoPrivateAccess, "pi"));
        assertEquals("null + 1", PrivateAccessor.invokeStatic(DemoPrivateAccess.class, "privateStaticFuncWithArgs", null, 1));
    }

}
