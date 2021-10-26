package com.alibaba.testable.agent.handler.test;

import java.util.Collections;
import java.util.List;

public class TestNgFramework extends CommonFramework {

    private static final String ANNOTATION_TEST = "Lorg/testng/annotations/Test;";
    private static final String ANNOTATION_CLEANUP = "Lorg/testng/annotations/AfterMethod;";

    @Override
    public List<String> getTestMethodAnnotations() {
        return Collections.singletonList(ANNOTATION_TEST);
    }

    @Override
    public String getCleanupMethodAnnotation() {
        return ANNOTATION_CLEANUP;
    }
}
