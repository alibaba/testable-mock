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
        String identify = getInvokeIdentify(mockMethodName, testClass, testCaseName);
        List<Object[]> records = getInvokeRecord(identify);
        if (isConstructor) {
            records.add(args);
            LogUtil.verbose("Mock constructor invoked \"%s\"", identify);
        } else {
            records.add(slice(args, 1));
            LogUtil.verbose("Mock method invoked \"%s\"", identify);
        }
        INVOKE_RECORDS.put(identify, records);
    }

    /**
     * Get identify key for mock invocation record
     * @param mockMethodName mock method name
     * @param testClass test class name
     * @param testCaseName test case name
     * @return identify key
     */
    public static String getInvokeIdentify(String mockMethodName, String testClass, String testCaseName) {
        return testClass + JOINER + testCaseName + JOINER + mockMethodName;
    }

    /**
     * Get mock method invoke count
     * @param identify key of invocation record
     * @return parameters used when specified method invoked in specified test case
     */
    public static List<Object[]> getInvokeRecord(String identify) {
        List<Object[]> records = INVOKE_RECORDS.get(identify);
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
