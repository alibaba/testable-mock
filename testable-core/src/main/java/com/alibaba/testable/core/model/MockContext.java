package com.alibaba.testable.core.model;

import java.util.HashMap;
import java.util.Map;

public class MockContext {

    public final String testClassName;

    public final String testCaseName;

    public final Map<String, Object> parameters;

    public MockContext(String testClassName, String testCaseName) {
        this.testClassName = testClassName;
        this.testCaseName = testCaseName;
        this.parameters = new HashMap<String, Object>();
    }
}
