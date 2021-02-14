package com.alibaba.testable.agent.handler.test;

public class TestNgFramework extends Framework {

    private static final String ANNOTATION_TEST = "Lorg/testng/annotations/Test;";
    private static final String ANNOTATION_AFTER_TEST = "Lorg/testng/annotations/AfterMethod;";

    @Override
    public String getTestAnnotation() {
        return ANNOTATION_TEST;
    }

    @Override
    public String getTestAfterAnnotation() {
        return ANNOTATION_AFTER_TEST;
    }
}
