package com.alibaba.testable.core.util;

import com.alibaba.testable.core.tool.PrivateAccessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CollectionUtilTest {

    @Test
    void should_slice_array() {
        Object[] args = new Object[]{"1", "2", "3"};
        Object[] slicedArgs = PrivateAccessor.invokeStatic(CollectionUtil.class, "slice", args, 1);
        assertEquals(2, slicedArgs.length);
        assertEquals("2", slicedArgs[0]);
        assertEquals("3", slicedArgs[1]);
    }
}
