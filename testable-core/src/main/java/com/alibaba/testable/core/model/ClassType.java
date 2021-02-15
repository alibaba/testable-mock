package com.alibaba.testable.core.model;

public enum ClassType {

    /**
     * it's a source class
     */
    SourceClass,

    /**
     * it's a test class
     */
    TestClass,

    /**
     * guess by whether class name ends with "Test"
     */
    GuessByName

}
