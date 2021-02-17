package com.alibaba.testable.core.util;

import com.alibaba.testable.core.model.MockContext;
import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.*;

import static com.alibaba.testable.core.constant.ConstPool.MOCK_POSTFIX;
import static com.alibaba.testable.core.constant.ConstPool.TEST_POSTFIX;

public class MockContextUtil {

    /**
     * Container to store context information of each test case
     */
    public static InheritableThreadLocal<MockContext> context = new TransmittableThreadLocal<MockContext>();

    /**
     * Mock class referred by @MockWith annotation to list of its test classes
     * MockClassName (dot-separated) → Set of associated [TestClassNames (dot-separated)]
     */
    public static Map<String, Set<String>> mockToTests = UnnullableMap.of(new HashSet<String>());

    /**
     * [0]Thread → [1]MockContextUtil → [2]TestClass/MockClass
     */
    public static final int INDEX_OF_INVOKER_CLASS = 2;

    /**
     * Initialize mock context
     * should be invoked at the beginning of each test case method
     */
    public static void init() {
        String testClassName = Thread.currentThread().getStackTrace()[INDEX_OF_INVOKER_CLASS].getClassName();
        String testCaseName = Thread.currentThread().getStackTrace()[INDEX_OF_INVOKER_CLASS].getMethodName();
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

    /**
     * Check whether current mock method is invoked from its associated test class
     * should be invoked in mock method
     */
    public static boolean isAssociated() {
        MockContext mockContext = context.get();
        String testClassName = (mockContext == null) ? "" : mockContext.testClassName;
        String mockClassName = Thread.currentThread().getStackTrace()[INDEX_OF_INVOKER_CLASS].getClassName();
        return isAssociatedByInnerMockClass(testClassName, mockClassName) ||
            isAssociatedByOuterMockClass(testClassName, mockClassName) ||
            isAssociatedByMockWithAnnotation(testClassName, mockClassName);
    }

    private static boolean isAssociatedByInnerMockClass(String testClassName, String mockClassName) {
        return mockClassName.equals(String.format("%s$%s", testClassName, MOCK_POSTFIX));
    }

    private static boolean isAssociatedByOuterMockClass(String testClassName, String mockClassName) {
        return testClassName.endsWith(TEST_POSTFIX) &&
            mockClassName.equals(testClassName.substring(0, testClassName.length() - 4) + MOCK_POSTFIX);
    }

    private static boolean isAssociatedByMockWithAnnotation(String testClassName, String mockClassName) {
        return mockToTests.get(mockClassName).contains(testClassName);
    }

}
