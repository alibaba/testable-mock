package com.alibaba.testable.core.util;

import com.alibaba.testable.core.constant.ConstPool;

/**
 * @author flin
 */
public class TestableUtil {

    public static String sourceMemberMethodName(Object testClassRef) {
        return sourceMemberMethodName(testClassRef.getClass());
    }

    public static String sourceMemberMethodName(Class<?> testClass) {
        StackTraceElement[] stack = getMainThread().getStackTrace();
        String testClassName = getRealClassName(testClass);
        String sourceClassName = testClassName.substring(0, testClassName.length() - ConstPool.TEST_POSTFIX.length());
        for (int i = stack.length - 1; i >= 0; i--) {
            if (stack[i].getClassName().equals(sourceClassName)) {
                return stack[i].getMethodName();
            }
        }
        return "";
    }

    public static String currentTestCaseName(Object testClassRef) {
        return currentTestCaseName(testClassRef.getClass());
    }

    public static String currentTestCaseName(Class<?> testClass) {
        StackTraceElement[] stack = getMainThread().getStackTrace();
        String testClassName = getRealClassName(testClass);
        for (int i = stack.length - 1; i >= 0; i--) {
            if (stack[i].getClassName().equals(testClassName)) {
                return stack[i].getMethodName();
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
        return Thread.currentThread();
    }

}
