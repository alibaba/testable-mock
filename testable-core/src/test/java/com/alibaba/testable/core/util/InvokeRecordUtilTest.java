package com.alibaba.testable.core.util;

import com.alibaba.testable.core.accessor.PrivateAccessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvokeRecordUtilTest {

    @Test
    void should_slice_array() {
        Object[] args = new Object[]{"1", "2", "3"};
        Object[] slicedArgs = PrivateAccessor.invokeStatic(InvokeRecordUtil.class, "slice", args, 1);
        assertEquals(2, slicedArgs.length);
        assertEquals("2", slicedArgs[0]);
        assertEquals("3", slicedArgs[1]);
    }
}
