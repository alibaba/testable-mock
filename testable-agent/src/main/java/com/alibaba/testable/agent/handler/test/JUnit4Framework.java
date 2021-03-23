package com.alibaba.testable.agent.handler.test;

import java.util.Collections;
import java.util.List;

public class JUnit4Framework extends Framework {

    public static final String ANNOTATION_TEST = "Lorg/junit/Test;";
    private static final String ANNOTATION_AFTER_TEST = "Lorg/junit/After;";

    @Override
    public List<String> getTestAnnotations() {
        return Collections.singletonList(ANNOTATION_TEST);
    }

    @Override
    public String getTestAfterAnnotation() {
        return ANNOTATION_AFTER_TEST;
    }
}
