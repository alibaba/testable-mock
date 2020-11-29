package com.alibaba.testable.core.util;

import java.util.Set;

/**
 * @author flin
 */
public class TestableUtil {

    /**
     * [0]Thread.getStackTrace() → [1]currentSourceMethodName() → [2]MockMethod -> [3]SourceMethod
     */
    private static final int INDEX_OF_SOURCE_METHOD = 3;
    /**
     * [0]Thread.getStackTrace() → [1]previousStackLocation() → [2]Invoker -> [3]Caller of invoker
     */
    private static final int INDEX_OF_CALLER_METHOD = 3;
    /**
     * Just a special number to identify test worker thread
     */
    private static final int TEST_WORKER_THREAD_PRIORITY = 55555;

    /**
     * Get the last visit method in source file
     * @param testClassRef usually `this` variable of the test class
     * @return method name
     */
    public static String currentSourceMethodName(Object testClassRef) {
        return Thread.currentThread().getStackTrace()[INDEX_OF_SOURCE_METHOD].getMethodName();
    }

    /**
     * Get current test case method
     * @param testClassRef usually `this` variable of the test class
     * @return method name
     */
    public static String currentTestCaseName(Object testClassRef) {
        Class<?> testClass = testClassRef.getClass();
        String testClassName = getOuterClassName(testClass.getName());
        return currentTestCaseName(testClassName);
    }

    /**
     * Get current test case method
     * @param testClassName name of current test class
     * @return method name
     */
    public static String currentTestCaseName(String testClassName) {
        // try current thread
        String testCaseName = findFirstMethodFromTestClass(testClassName, Thread.currentThread().getStackTrace());
        if (testCaseName.isEmpty()) {
            Set<Thread> threads = Thread.getAllStackTraces().keySet();
            // try find previously marked thread
            Thread testWorkerThread = findTestWorkerThread(threads);
            if (testWorkerThread != null) {
                testCaseName = findFirstMethodFromTestClass(testClassName, testWorkerThread.getStackTrace());
                if (!testCaseName.isEmpty()) {
                    return testCaseName;
                }
            }
            // travel all possible threads
            for (Thread t : threads) {
                testCaseName = findFirstMethodFromTestClass(testClassName, t.getStackTrace());
                if (!testCaseName.isEmpty()) {
                    t.setPriority(TEST_WORKER_THREAD_PRIORITY);
                    return testCaseName;
                }
            }
        }
        System.err.println("testCaseName: " + testCaseName);
        return testCaseName;
    }

    /**
     * Get file name and line number of where current method was called
     * @return in "filename:linenumber" format
     */
    public static String previousStackLocation() {
        StackTraceElement stack = Thread.currentThread().getStackTrace()[INDEX_OF_CALLER_METHOD];
        return stack.getFileName() + ":" + stack.getLineNumber();
    }

    private static Thread findTestWorkerThread(Set<Thread> threads) {
        for (Thread t : threads) {
            if (t.getPriority() == TEST_WORKER_THREAD_PRIORITY) {
                return t;
            }
        }
        return null;
    }

    private static String findFirstMethodFromTestClass(String testClassName, StackTraceElement[] stack) {
        for (int i = stack.length - 1; i >= 0; i--) {
            if (getOuterClassName(stack[i].getClassName()).equals(testClassName)) {
                return stack[i].getClassName().indexOf('$') > 0 ?
                    // test case using async call
                    getMethodNameFromLambda(stack[i].getClassName()) :
                    // in case of lambda method
                    getMethodNameFromLambda(stack[i].getMethodName());
            }
        }
        return "";
    }

    private static String getMethodNameFromLambda(String originName) {
        int beginOfMethodName = originName.indexOf('$');
        if (beginOfMethodName < 0) {
            return originName;
        }
        int endOfMethodName = originName.indexOf('$', beginOfMethodName + 1);
        return originName.substring(beginOfMethodName + 1, endOfMethodName);
    }

    private static String getOuterClassName(String className) {
        int posOfInnerClass = className.indexOf('$');
        return posOfInnerClass > 0 ? className.substring(0, posOfInnerClass) : className;
    }

}
