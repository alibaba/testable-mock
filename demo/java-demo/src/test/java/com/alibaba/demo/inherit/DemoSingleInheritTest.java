package com.alibaba.demo.inherit;

import com.alibaba.testable.core.annotation.MockInvoke;
import org.junit.jupiter.api.Test;

import static com.alibaba.testable.core.matcher.InvocationVerifier.verifyInvoked;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 演示Mock容器类的继承
 * Demonstrate inherit of mock container class with extends keyword
 */
class DemoSingleInheritTest {

    private DemoSingleInherit demoSingleInherit = new DemoSingleInherit();

    public static class BasicMock {
        @MockInvoke(targetClass = DemoSingleInherit.class)
        private String suffix() {
            return "_ck";
        }
    }

    public static class Mock extends BasicMock {
        @MockInvoke(targetClass = DemoSingleInherit.class)
        private String prefix() {
            return "mo_";
        }
    }

    @Test
    public void should_use_mock_method_in_parent_class() {
        assertEquals("mo_test_ck", demoSingleInherit.entry("test"));
        verifyInvoked("prefix").withTimes(1);
        verifyInvoked("suffix").withTimes(1);
    }

}