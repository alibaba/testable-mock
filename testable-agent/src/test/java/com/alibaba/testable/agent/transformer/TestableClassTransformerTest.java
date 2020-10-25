package com.alibaba.testable.agent.transformer;

import com.alibaba.testable.core.accessor.PrivateAccessor;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.AnnotationNode;

import static com.alibaba.testable.agent.util.CollectionUtil.listOf;
import static org.junit.jupiter.api.Assertions.*;

class TestableClassTransformerTest {

    private TestableClassTransformer transformer = new TestableClassTransformer();

    @Test
    void should_get_annotation_parameter() {
        AnnotationNode an = new AnnotationNode("");
        an.values = listOf((Object)"testKey", "testValue", "demoKey", "demoValue");
        assertEquals("testValue", PrivateAccessor.invoke(transformer, "getAnnotationParameter", an, "testKey", "none"));
        assertEquals("demoValue", PrivateAccessor.invoke(transformer, "getAnnotationParameter", an, "demoKey", "none"));
        assertEquals("none", PrivateAccessor.invoke(transformer, "getAnnotationParameter", an, "testValue", "none"));
    }

}
