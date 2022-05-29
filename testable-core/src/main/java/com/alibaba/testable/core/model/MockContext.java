package com.alibaba.testable.core.model;

import com.alibaba.testable.core.util.UnnullableMap;

import java.util.*;

public class MockContext {

    /**
     * current test class (dot separated) name
     */
    public final String testClassName;

    /**
     * current test case name
     */
    public final String testCaseName;

    /**
     * store of MOCK_CONTEXT.get() and MOCK_CONTEXT.set()
     */
    public final Map<String, Object> parameters;

    /**
     * invocation record of mock method and arguments
     */
    public final Map<String, List<Object[]>> invokeRecord;

    public MockContext(String testClassName, String testCaseName) {
        this.testClassName = testClassName;
        this.testCaseName = testCaseName;
        this.parameters = new HashMap<String, Object>();
        this.invokeRecord = UnnullableMap.of(new ArrayList<Object[]>());
    }
}
