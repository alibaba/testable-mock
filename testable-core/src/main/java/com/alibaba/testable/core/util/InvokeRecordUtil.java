package com.alibaba.testable.core.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author flin
 */
public class InvokeRecordUtil {

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

    public static int getInvokeCount(String mockMethodName, String testCaseName) {
        String key = testCaseName + JOINER + mockMethodName;
        Integer count = INVOKE_RECORDS.get(key);
        if (count == null) {
            count = 0;
        }
        return count;
    }

}
