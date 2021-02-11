package com.alibaba.testable.agent.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author flin
 */
public class MockContext {

    /**
     * Mock method name → ( key → value )
     */
    public static final Map<String, HashMap<String, Object>> parameters = UnnullableMap.of(new HashMap<String, Object>());

}
