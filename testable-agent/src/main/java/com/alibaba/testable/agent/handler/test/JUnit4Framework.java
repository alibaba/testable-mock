package com.alibaba.testable.agent.handler.test;

public class JUnit4Framework extends Framework {

    private static final String ANNOTATION_TEST = "Lorg/junit/Test;";
    private static final String ANNOTATION_AFTER_TEST = "Lorg/junit/After;";

    @Override
    public String getTestAnnotation() {
        return ANNOTATION_TEST;
    }

    @Override
    public String getTestAfterAnnotation() {
        return ANNOTATION_AFTER_TEST;
    }
}
