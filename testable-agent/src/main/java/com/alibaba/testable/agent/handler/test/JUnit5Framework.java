package com.alibaba.testable.agent.handler.test;

public class JUnit5Framework extends Framework {

    private static final String ANNOTATION_TEST = "Lorg/junit/jupiter/api/Test;";
    private static final String ANNOTATION_AFTER_TEST = "Lorg/junit/jupiter/api/AfterEach;";

    @Override
    public String getTestAnnotation() {
        return ANNOTATION_TEST;
    }

    @Override
    public String getTestAfterAnnotation() {
        return ANNOTATION_AFTER_TEST;
    }
}
