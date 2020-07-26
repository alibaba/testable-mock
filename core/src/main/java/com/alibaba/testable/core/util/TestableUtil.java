package com.alibaba.testable.core.util;

/**
 * @author flin
 */
public class TestableUtil {

    private static final String TESTABLE_NE = "n.e";

    public static String currentMemberMethodName() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (int i = 0; i < stack.length; i++) {
            if (stack[i].getClassName().equals(TESTABLE_NE)) {
                return stack[i + 1].getMethodName();
            }
        }
        return "";
    }

    public static String currentTestCaseName(Object testClassRef) {
        return currentTestCaseName(testClassRef.getClass());
    }

    public static String currentTestCaseName(Class testClass) {
        StackTraceElement[] stack = getMainThread().getStackTrace();
        String testClassName = getRealClassName(testClass);
        for (int i = stack.length - 1; i >= 0; i--) {
            if (stack[i].getClassName().equals(testClassName)) {
                return stack[i].getMethodName();
            }
        }
        return "";
    }

    private static String getRealClassName(Class testClass) {
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
