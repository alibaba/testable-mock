package com.alibaba.testable.core.util;

import com.alibaba.testable.core.model.MockContext;
import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.Map;

public class MockContextUtil {

    public static InheritableThreadLocal<MockContext> context = new TransmittableThreadLocal<MockContext>();

    /**
     * Should be invoked at the beginning of each test case method
     */
    public static void init(String testClassName, String testCaseName) {
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
