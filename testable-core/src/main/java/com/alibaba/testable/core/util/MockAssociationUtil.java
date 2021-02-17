package com.alibaba.testable.core.util;

import com.alibaba.testable.core.model.MockContext;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.alibaba.testable.core.constant.ConstPool.MOCK_POSTFIX;
import static com.alibaba.testable.core.constant.ConstPool.TEST_POSTFIX;

public class MockAssociationUtil {

    /**
     * [0]Thread → [1]MockAssociationUtil → [2]MockClass
     */
    public static final int INDEX_OF_MOCK_CLASS = 2;

    /**
     * Mock class referred by @MockWith annotation to list of its test classes
     * MockClassName (dot-separated) → Set of associated [TestClassNames (dot-separated)]
     */
    public static Map<String, Set<String>> mockToTests = UnnullableMap.of(new HashSet<String>());

    /**
     * Check whether current mock method is invoked from its associated test class
     * should be invoked in mock method
     */
    public static boolean isAssociated() {
        MockContext mockContext = MockContextUtil.context.get();
        String testClassName = (mockContext == null) ? "" : mockContext.testClassName;
        String mockClassName = Thread.currentThread().getStackTrace()[INDEX_OF_MOCK_CLASS].getClassName();
        return isAssociatedByInnerMockClass(testClassName, mockClassName) ||
            isAssociatedByOuterMockClass(testClassName, mockClassName) ||
            isAssociatedByMockWithAnnotation(testClassName, mockClassName);
    }

    public static Object invokeOrigin(String originClass, String originMethod, Object originObj, Object... args) {
        return null;
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
