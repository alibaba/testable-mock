package com.alibaba.testable.agent.transformer;

import com.alibaba.testable.agent.tool.ImmutablePair;
import com.alibaba.testable.core.accessor.PrivateAccessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestableClassTransformerTest {

    private TestableClassTransformer testableClassTransformer = new TestableClassTransformer();

    @Test
    void should_split_parameters() {
        ImmutablePair<String, String> parameters =
            PrivateAccessor.invoke(testableClassTransformer, "extractFirstParameter", "()");
        assertNull(parameters);
        parameters = PrivateAccessor.invoke(testableClassTransformer, "extractFirstParameter", "(Lcom.alibaba.demo.Class;ILjava.lang.String;Z)");
        assertNotNull(parameters);
        assertEquals("com.alibaba.demo.Class", parameters.left);
        assertEquals("(ILjava.lang.String;Z)", parameters.right);
    }
}
