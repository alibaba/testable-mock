package com.alibaba.testable.agent.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CollectionUtilTest {

    @Test
    void should_check_collection_contains_any_element() {
        assertTrue(CollectionUtil.containsAny(
            CollectionUtil.listOf("a", "b"), CollectionUtil.listOf("b", "c")
        ));
        assertFalse(CollectionUtil.containsAny(
            CollectionUtil.listOf("a", "b"), CollectionUtil.listOf("c", "d")
        ));
    }

}
