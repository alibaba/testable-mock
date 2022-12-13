package com.alibaba.testable.core.tool;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CollectionToolTest {

    @Test
    void should_slice_array() {
        Object[] args = new Object[]{"1", "2", "3", "4"};
        Object[] slicedArgs = CollectionTool.slice(args, 1);
        assertEquals(3, slicedArgs.length);
        assertEquals("2", slicedArgs[0]);
        assertEquals("3", slicedArgs[1]);
        assertEquals("4", slicedArgs[2]);
        slicedArgs = CollectionTool.slice(args, 1, 2);
        assertEquals(2, slicedArgs.length);
        assertEquals("2", slicedArgs[0]);
        assertEquals("3", slicedArgs[1]);
    }

}