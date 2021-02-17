package com.alibaba.testable.core.util;

import com.alibaba.testable.core.model.MockContext;
import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.*;

public class MockContextUtil {

    /**
     * Container to store context information of each test case
     */
    public static InheritableThreadLocal<MockContext> context = new TransmittableThreadLocal<MockContext>();

    /**
     * [0]Thread → [1]MockContextUtil → [2]TestClass
     */
    public static final int INDEX_OF_TEST_CLASS = 2;

    /**
     * Initialize mock context
     * should be invoked at the beginning of each test case method
     */
    public static void init() {
        String testClassName = Thread.currentThread().getStackTrace()[INDEX_OF_TEST_CLASS].getClassName();
        String testCaseName = Thread.currentThread().getStackTrace()[INDEX_OF_TEST_CLASS].getMethodName();
        context.set(new MockContext(testClassName, testCaseName));
    }

    /**
     * Clean up mock context
     * should be invoked at the end of each test case execution
     */
    public static void clean() {
        context.remove();
    }

    public static Map<String, Object> parameters() {
        MockContext mockContext = MockContextUtil.context.get();
        return mockContext == null ? new HashMap<String, Object>() : mockContext.parameters;
    }

}
