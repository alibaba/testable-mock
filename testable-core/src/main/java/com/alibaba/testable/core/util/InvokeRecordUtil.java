package com.alibaba.testable.core.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author flin
 */
public class InvokeRecordUtil {

    /**
     * Mock method name → List of invoke parameters
     */
    private static final Map<String, List<Object[]>> INVOKE_RECORDS = new HashMap<String, List<Object[]>>();
    private final static String JOINER = "::";

    /**
     * [0]Thread → [1]TestableUtil/TestableTool → [2]TestClass
     */
    public static final int INDEX_OF_TEST_CLASS = 2;

    /**
     * Record mock method invoke event
     * @param args invocation parameters
     * @param isConstructor whether mocked method is constructor
     */
    public static void recordMockInvoke(Object[] args, boolean isConstructor) {
        StackTraceElement mockMethodTraceElement = Thread.currentThread().getStackTrace()[INDEX_OF_TEST_CLASS];
        String mockMethodName = mockMethodTraceElement.getMethodName();
        String testClass = mockMethodTraceElement.getClassName();
        String testCaseName = TestableUtil.currentTestCaseName(testClass);
        String key = testCaseName + JOINER + mockMethodName;
        List<Object[]> records = getInvokeRecord(mockMethodName, testCaseName);
        if (isConstructor) {
            records.add(args);
        } else {
            records.add(slice(args, 1));
        }
        INVOKE_RECORDS.put(key, records);
    }

    /**
     * Get mock method invoke count
     * @param mockMethodName mock method name
     * @param testCaseName test case name
     * @return parameters used when specified method invoked in specified test case
     */
    public static List<Object[]> getInvokeRecord(String mockMethodName, String testCaseName) {
        String key = testCaseName + JOINER + mockMethodName;
        List<Object[]> records = INVOKE_RECORDS.get(key);
        return (records == null) ? new LinkedList<Object[]>() : records;
    }

    private static Object[] slice(Object[] args, int firstIndex) {
        int size = args.length - firstIndex;
        if (size <= 0) {
            return new Object[0];
        }
        Object[] slicedArgs = new Object[size];
        System.arraycopy(args, firstIndex, slicedArgs, 0, size);
        return slicedArgs;
    }

}
