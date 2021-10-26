package com.alibaba.testable.agent.handler.test;

import java.util.Arrays;
import java.util.List;

public class JUnit5Framework extends CommonFramework {

    public static final String ANNOTATION_TEST = "Lorg/junit/jupiter/api/Test;";
    public static final String ANNOTATION_PARAMETERIZED_TEST = "Lorg/junit/jupiter/params/ParameterizedTest;";
    private static final String ANNOTATION_CLEANUP = "Lorg/junit/jupiter/api/AfterEach;";

    @Override
    public List<String> getTestMethodAnnotations() {
        return Arrays.asList(ANNOTATION_TEST, ANNOTATION_PARAMETERIZED_TEST);
    }

    @Override
    public String getCleanupMethodAnnotation() {
        return ANNOTATION_CLEANUP;
    }
}
