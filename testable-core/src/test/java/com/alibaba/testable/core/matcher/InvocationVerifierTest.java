package com.alibaba.testable.core.matcher;

import com.alibaba.testable.core.tool.OmniConstructor;
import com.alibaba.testable.core.tool.PrivateAccessor;
import org.junit.jupiter.api.Test;

import static com.alibaba.testable.core.tool.CollectionTool.listOf;
import static org.junit.jupiter.api.Assertions.*;

class InvocationVerifierTest {

    private InvocationVerifier invocationVerifier = OmniConstructor.newInstance(InvocationVerifier.class);

    @Test
    void should_matches_object() {
        assertTrue((Boolean)PrivateAccessor.invoke(invocationVerifier, "matches", "abc", "abc"));
        assertTrue((Boolean)PrivateAccessor.invoke(invocationVerifier, "matches", 1L, 1L));

        assertFalse((Boolean)PrivateAccessor.invoke(invocationVerifier, "matches", "xyz", "abc"));
        assertFalse((Boolean)PrivateAccessor.invoke(invocationVerifier, "matches", 1L, "abc"));
        assertFalse((Boolean)PrivateAccessor.invoke(invocationVerifier, "matches", 1L, 1));
    }

    @Test
    void should_matches_array() {
        assertTrue((Boolean)PrivateAccessor.invoke(invocationVerifier, "matches",
                new String[] {"abc", "xyz"}, new String[] {"abc", "xyz"}));

        assertFalse((Boolean)PrivateAccessor.invoke(invocationVerifier, "matches",
                new String[] {"abc", "xyz"}, new String[] {"xyz", "abc"}));
        assertFalse((Boolean)PrivateAccessor.invoke(invocationVerifier, "matches",
                new String[] {"abc", "xyz"}, new String[] {"xyz"}));
    }

    @Test
    void should_matches_matcher() {
        assertTrue((Boolean)PrivateAccessor.invoke(invocationVerifier, "matches",
                InvocationMatcher.anyArray(), new String[] {"abc", "xyz"}));
        assertTrue((Boolean)PrivateAccessor.invoke(invocationVerifier, "matches",
                InvocationMatcher.anyString(), "abc"));

        assertFalse((Boolean)PrivateAccessor.invoke(invocationVerifier, "matches",
                InvocationMatcher.anyString(), new String[] {"abc", "xyz"}));
        assertFalse((Boolean)PrivateAccessor.invoke(invocationVerifier, "matches",
                InvocationMatcher.anyArray(), "abc"));
    }
}