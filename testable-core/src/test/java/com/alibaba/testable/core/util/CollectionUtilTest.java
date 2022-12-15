package com.alibaba.testable.core.util;

import org.junit.jupiter.api.Test;

import static com.alibaba.testable.core.util.CollectionUtil.fastListOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectionUtilTest {

    @Test
    void should_check_collection_contains_any_element() {
        assertTrue(CollectionUtil.containsAny(
                fastListOf("a", "b"), fastListOf("b", "c")
        ));
        assertFalse(CollectionUtil.containsAny(
                fastListOf("a", "b"), fastListOf("c", "d")
        ));
    }
}
