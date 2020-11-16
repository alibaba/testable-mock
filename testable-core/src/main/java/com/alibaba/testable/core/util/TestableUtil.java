package com.alibaba.testable.core.util;

import com.alibaba.testable.core.constant.ConstPool;


/**
 * @author flin
 */
public class TestableUtil {

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

    /**
     * Get file name and line number of where current method was called
     */
    public static String getPreviousStackLocation() {
        // 0 - Thread.getStackTrace(), 1 - this method, 2 - code call this method, 3 - code call the caller method
        StackTraceElement stack = getMainThread().getStackTrace()[3];
        return stack.getFileName() + ":" + stack.getLineNumber();
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
