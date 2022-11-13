package com.alibaba.testable.agent.util;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.AnnotationNode;

import static com.alibaba.testable.core.util.CollectionUtil.listOf;
import static org.junit.jupiter.api.Assertions.*;

class AnnotationUtilTest {

    @Test
    void should_get_annotation_parameter() {
        AnnotationNode an = new AnnotationNode("");
        an.values = listOf((Object)"testKey", "testValue", "demoKey", "demoValue");
        assertEquals("testValue", AnnotationUtil.getAnnotationParameter(an, "testKey", "none", String.class));
        assertEquals("demoValue", AnnotationUtil.getAnnotationParameter(an, "demoKey", "none", String.class));
        assertEquals("none", AnnotationUtil.getAnnotationParameter(an, "testValue", "none", String.class));
    }

}
