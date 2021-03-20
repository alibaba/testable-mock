package com.alibaba.testable.agent.transformer;

import com.alibaba.testable.agent.tool.ImmutablePair;
import com.alibaba.testable.core.tool.PrivateAccessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MockClassParserTest {

    private MockClassParser mockClassParser = new MockClassParser();

    @Test
    void should_split_parameters() {
        ImmutablePair<String, String> parameters =
            PrivateAccessor.invoke(mockClassParser, "extractFirstParameter", "()");
        assertNull(parameters);
        parameters = PrivateAccessor.invoke(mockClassParser, "extractFirstParameter", "(Lcom.alibaba.demo.Class;ILjava.lang.String;Z)");
        assertNotNull(parameters);
        assertEquals("com.alibaba.demo.Class", parameters.left);
        assertEquals("(ILjava.lang.String;Z)", parameters.right);
    }
}
