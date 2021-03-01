package com.alibaba.testable.agent.handler.test;

import java.util.Arrays;
import java.util.List;

public class JUnit5Framework extends Framework {

    private static final String ANNOTATION_TEST = "Lorg/junit/jupiter/api/Test;";
    private static final String ANNOTATION_PARAMETERIZED_TEST = "Lorg/junit/jupiter/params/ParameterizedTest;";
    private static final String ANNOTATION_AFTER_TEST = "Lorg/junit/jupiter/api/AfterEach;";

    @Override
    public List<String> getTestAnnotations() {
        return Arrays.asList(ANNOTATION_TEST, ANNOTATION_PARAMETERIZED_TEST);
    }

    @Override
    public String getTestAfterAnnotation() {
        return ANNOTATION_AFTER_TEST;
    }
}
