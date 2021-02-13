package com.alibaba.testable.agent.model;

import com.alibaba.testable.core.util.UnnullableMap;

import java.util.HashMap;
import java.util.Map;

/**
 * @author flin
 */
public class MockContextHolder {

    /**
     * Mock method name → ( key → value )
     */
    public static final Map<String, HashMap<String, Object>> parameters = UnnullableMap.of(new HashMap<String, Object>());

}
