package com.alibaba.testable.core.tool;

import com.alibaba.testable.core.util.TestableUtil;

/**
 * @author flin
 */
public class TestableTool {

    /**
     * Name of the constructor method
     */
    public static final String CONSTRUCTOR = "<init>";

    /**
     * Name of current test case method
     */
    public static String TEST_CASE;

    /**
     * Name of the last visited method in source class
     */
    public static String SOURCE_METHOD;

    /**
     * Get counter to check whether specified mock method invoked
     * @param mockMethodName name of a mock method
     */
    public static InvokeCounter verify(String mockMethodName) {
        String testClass = Thread.currentThread().getStackTrace()[TestableUtil.INDEX_OF_TEST_CLASS].getClassName();
        String testCaseName = TestableUtil.currentTestCaseName(testClass);
        return new InvokeCounter(TestableUtil.getInvokeCount(mockMethodName, testCaseName));
    }

}
