package com.alibaba.testable.core.util;

import com.alibaba.testable.core.model.MockContext;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.alibaba.testable.core.constant.ConstPool.*;
import static com.alibaba.testable.core.tool.PrivateAccessor.*;

public class MockAssociationUtil {

    /**
     * [0]Thread → [1]MockAssociationUtil → [2]MockClass
     */
    public static final int INDEX_OF_MOCK_CLASS = 2;

    /**
     * Sub-class of specified mock class
     * SuperMockClassName (dot-separated) → Set of [SubMockClassName (dot-separated)]
     */
    public static Map<String, Set<String>> subMockContainers = UnnullableMap.of(new HashSet<String>());

    /**
     * Mock class referred by @MockWith annotation to list of its test classes
     * MockClassName (dot-separated) → Set of associated [TestClassNames (dot-separated)]
     */
    public static Map<String, Set<String>> mockToTests = UnnullableMap.of(new HashSet<String>());

    /**
     * Note: this method will be invoked in transformed byte code
     * Check whether current mock method is invoked from its associated test class
     * should be invoked in mock method
     */
    public static boolean isAssociated() {
        MockContext mockContext = MockContextUtil.context.get();
        if (mockContext == null) {
            // invoked from test case not transformed by testable
            return false;
        }
        String testClassName = mockContext.testClassName;
        String mockClassName = Thread.currentThread().getStackTrace()[INDEX_OF_MOCK_CLASS].getClassName();
        return recursiveAssociationCheck(testClassName, mockClassName);
    }

    private static boolean recursiveAssociationCheck(String testClassName, String mockClassName) {
        return isAssociatedByInnerMockClass(testClassName, mockClassName) ||
            isAssociatedByOuterMockClass(testClassName, mockClassName) ||
            isAssociatedByMockWithAnnotation(testClassName, mockClassName) ||
            (subMockContainers.containsKey(mockClassName) &&
                recursiveAssociationCheck(testClassName, subMockContainers.get(mockClassName)));
    }

    private static boolean recursiveAssociationCheck(String testClassName, Set<String> mockClassNames) {
        for (String name : mockClassNames) {
            if (recursiveAssociationCheck(testClassName, name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Note: this method will be invoked in transformed byte code
     * Invoke original method when mock class is not associated with current test case
     */
    public static Object invokeOrigin(Class<?> originClass, String originMethod, Object... args) {
        if (originMethod.equals(CONSTRUCTOR)) {
            return construct(originClass, args);
        } else if (args[0] == null) {
            return invokeStatic(originClass, originMethod, CollectionUtil.slice(args, 1));
        } else {
            return invoke(args[0], originMethod, CollectionUtil.slice(args, 1));
        }
    }

    public static void recordSubMockContainer(String superClassName, String subClassName) {
        subMockContainers.get(superClassName).add(subClassName);
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
