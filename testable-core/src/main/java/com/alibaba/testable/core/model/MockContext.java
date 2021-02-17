package com.alibaba.testable.core.model;

import com.alibaba.testable.core.util.UnnullableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockContext {

    /**
     * dot separated class name
     */
    public final String testClassName;

    public final String testCaseName;

    public final Map<String, Object> parameters;

    public final Map<String, List<Object[]>> invokeRecord;

    public MockContext(String testClassName, String testCaseName) {
        this.testClassName = testClassName;
        this.testCaseName = testCaseName;
        this.parameters = new HashMap<String, Object>();
        this.invokeRecord = UnnullableMap.of(new ArrayList<Object[]>());
    }
}
