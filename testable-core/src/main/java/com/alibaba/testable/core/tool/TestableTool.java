package com.alibaba.testable.core.tool;

import java.util.Map;

/**
 * @author flin
 */
public class TestableTool {

    /**
     * Name of the last visited method in source class
     */
    public static String SOURCE_METHOD;

    /**
     * Inject extra mock parameters
     */
    public static Map<String, Object> MOCK_CONTEXT;

}
