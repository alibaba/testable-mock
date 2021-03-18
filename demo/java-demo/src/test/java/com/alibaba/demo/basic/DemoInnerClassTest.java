package com.alibaba.demo.basic;

import com.alibaba.testable.core.annotation.MockMethod;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 演示对内部类的Mock支持
 * Demonstrate support for mocking invocation inside a inner class
 */
class DemoInnerClassTest {

    public static class Mock {
        @MockMethod(targetClass = DemoInnerClass.class)
        String methodToBeMock() {
            return "MockedCall";
        }
    }

    @Test
    void should_mock_invoke_inside_inner_class() throws Exception {
        DemoInnerClass demo = new DemoInnerClass();
        assertEquals("MockedCall", demo.callInnerDemo());
        assertEquals("MockedCall", demo.callAnonymousInner());
        assertEquals("MockedCall", demo.callLambdaInner());
        assertEquals("MockedCall", new DemoInnerClass.StaticInner().demo());
    }
}
