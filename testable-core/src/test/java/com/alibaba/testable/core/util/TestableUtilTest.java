package com.alibaba.testable.core.util;

import com.alibaba.testable.core.accessor.PrivateAccessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestableUtilTest {

    @Test
    void should_get_outer_class_name() {
        assertEquals("com.alibaba.testable.demo.DemoMockTest",
            PrivateAccessor.<String>invokeStatic(TestableUtil.class, "getOuterClassName", "com.alibaba.testable.demo.DemoMockTest$should_able_to_get_source_method_name$1"));
    }

    @Test
    void should_get_method_name_from_lambda_class_or_method() {
        assertEquals("should_able_to_get_source_method_name",
            PrivateAccessor.<String>invokeStatic(TestableUtil.class, "getMethodNameFromLambda", "com.alibaba.testable.demo.DemoMockTest$should_able_to_get_source_method_name$1"));
        assertEquals("should_able_to_get_source_method_name",
            PrivateAccessor.<String>invokeStatic(TestableUtil.class, "getMethodNameFromLambda", "lambda$should_able_to_get_source_method_name$0"));
    }
}
