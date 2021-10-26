package com.alibaba.testable.agent.handler.test;

import java.util.Collections;
import java.util.List;

public class JUnit4Framework extends CommonFramework {

    public static final String ANNOTATION_TEST = "Lorg/junit/Test;";
    private static final String ANNOTATION_CLEANUP = "Lorg/junit/After;";

    @Override
    public List<String> getTestMethodAnnotations() {
        return Collections.singletonList(ANNOTATION_TEST);
    }

    @Override
    public String getCleanupMethodAnnotation() {
        return ANNOTATION_CLEANUP;
    }
}
