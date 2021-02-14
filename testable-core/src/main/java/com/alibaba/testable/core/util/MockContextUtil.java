package com.alibaba.testable.core.util;

import com.alibaba.testable.core.model.MockContext;
import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.*;

public class MockContextUtil {

    public static InheritableThreadLocal<MockContext> context = new TransmittableThreadLocal<MockContext>();

    /**
     * mock class referred by @MockWith annotation to list of its test classes
     */
    public static Map<String, Set<String>> mockToTests = UnnullableMap.of(new HashSet<String>());

    /**
     * [0]Thread → [1]MockContextUtil → [2]TestClass
     */
    public static final int INDEX_OF_TEST_CLASS = 2;

    /**
     * Should be invoked at the beginning of each test case method
     */
    public static void init() {
        String testClassName = Thread.currentThread().getStackTrace()[INDEX_OF_TEST_CLASS].getClassName();
        String testCaseName = Thread.currentThread().getStackTrace()[INDEX_OF_TEST_CLASS].getMethodName();
        context.set(new MockContext(testClassName, testCaseName));
    }

    /**
     * Should be invoked at the end of each test case execution
     */
    public static void clean() {
        context.remove();
    }

    public static Map<String, Object> parameters() {
        return MockContextUtil.context.get().parameters;
    }

}
