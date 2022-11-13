package com.alibaba.testable.core.util;

import org.junit.jupiter.api.Test;

import static com.alibaba.testable.core.tool.PrivateAccessor.invokeStatic;
import static org.junit.jupiter.api.Assertions.*;

class CollectionUtilTest {

    @Test
    void should_slice_array() {
        Object[] args = new Object[]{"1", "2", "3", "4"};
        Object[] slicedArgs = CollectionUtil.slice(args, 1);
        assertEquals(3, slicedArgs.length);
        assertEquals("2", slicedArgs[0]);
        assertEquals("3", slicedArgs[1]);
        assertEquals("4", slicedArgs[2]);
        slicedArgs = CollectionUtil.slice(args, 1, 2);
        assertEquals(2, slicedArgs.length);
        assertEquals("2", slicedArgs[0]);
        assertEquals("3", slicedArgs[1]);
    }

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
