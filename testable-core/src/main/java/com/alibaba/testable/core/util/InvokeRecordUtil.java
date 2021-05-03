package com.alibaba.testable.core.util;

import com.alibaba.testable.core.model.MockContext;

/**
 * @author flin
 */
public class InvokeRecordUtil {

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
        MockContext mockContext = MockContextUtil.context.get();
        if (mockContext == null) {
            // mock method not invoked from test case, e.g. in static block
            return;
        }
        String testClass = mockContext.testClassName;
        String testCaseName = mockContext.testCaseName;
        if (isConstructor) {
            mockContext.invokeRecord.get(mockMethodName).add(args);
            LogUtil.verbose("   Mock constructor \"%s\" invoked in %s::%s", mockMethodName, testClass, testCaseName);
        } else {
            mockContext.invokeRecord.get(mockMethodName).add(CollectionUtil.slice(args, 1));
            LogUtil.verbose("   Mock method \"%s\" invoked in %s::%s\"", mockMethodName, testClass, testCaseName);
        }
    }

}
