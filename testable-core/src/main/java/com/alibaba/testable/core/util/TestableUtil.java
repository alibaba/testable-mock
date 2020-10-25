package com.alibaba.testable.core.util;

import com.alibaba.testable.core.constant.ConstPool;

import java.util.HashMap;
import java.util.Map;

/**
 * @author flin
 */
public class TestableUtil {

    private static final Map<String, Integer> INVOKE_RECORDS = new HashMap<String, Integer>();
    private final static String JOINER = "->";
    /**
     * [0]Thread -> [1]TestableUtil/TestableTool -> [2]TestClass
     */
    public static final int INDEX_OF_TEST_CLASS = 2;

    /**
     * Record mock method invoke event
     */
    public static void countMockInvoke() {
        StackTraceElement mockMethodTraceElement = Thread.currentThread().getStackTrace()[INDEX_OF_TEST_CLASS];
        String mockMethodName = mockMethodTraceElement.getMethodName();
        String testClass = mockMethodTraceElement.getClassName();
        String testCaseName = TestableUtil.currentTestCaseName(testClass);
        String key = testCaseName + JOINER + mockMethodName;
        int count = getInvokeCount(mockMethodName, testCaseName);
        INVOKE_RECORDS.put(key, count + 1);
    }

    /**
     * Get the last visit method in source file
     * @param testClassRef usually `this` variable of the test class
     * @return method name
     */
    public static String currentSourceMethodName(Object testClassRef) {
        Class<?> testClass = testClassRef.getClass();
        String testClassName = getRealClassName(testClass);
        String sourceClassName = testClassName.substring(0, testClassName.length() - ConstPool.TEST_POSTFIX.length());
        String sourceMethod = findLastMethodFromSourceClass(sourceClassName, getMainThread().getStackTrace());
        if (sourceMethod.isEmpty()) {
            return findLastMethodFromSourceClass(sourceClassName, Thread.currentThread().getStackTrace());
        }
        return sourceMethod;
    }

    /**
     * Get current test case method
     * @param testClassRef usually `this` variable of the test class
     * @return method name
     */
    public static String currentTestCaseName(Object testClassRef) {
        Class<?> testClass = testClassRef.getClass();
        String testClassName = getRealClassName(testClass);
        return currentTestCaseName(testClassName);
    }

    /**
     * Get current test case method
     * @param testClassName name of current test class
     * @return method name
     */
    public static String currentTestCaseName(String testClassName) {
        StackTraceElement[] stack = getMainThread().getStackTrace();
        for (int i = stack.length - 1; i >= 0; i--) {
            if (stack[i].getClassName().equals(testClassName)) {
                return stack[i].getMethodName();
            }
        }
        return "";
    }

    public static int getInvokeCount(String mockMethodName, String testCaseName) {
        String key = testCaseName + JOINER + mockMethodName;
        Integer count = INVOKE_RECORDS.get(key);
        if (count == null) {
            count = 0;
        }
        return count;
    }

    private static String findLastMethodFromSourceClass(String sourceClassName, StackTraceElement[] stack) {
        for (StackTraceElement element : stack) {
            if (element.getClassName().equals(sourceClassName)) {
                return element.getMethodName();
            }
        }
        return "";
    }

    private static String getRealClassName(Class<?> testClass) {
        String className = testClass.getName();
        int posOfInnerClass = className.lastIndexOf('$');
        return posOfInnerClass > 0 ? className.substring(0, posOfInnerClass) : className;
    }

    private static Thread getMainThread() {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getId() == 1L) {
                return t;
            }
        }
        // usually impossible to go here
        return Thread.currentThread();
    }

}
