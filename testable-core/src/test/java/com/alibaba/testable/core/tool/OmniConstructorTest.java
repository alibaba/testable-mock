package com.alibaba.testable.core.tool;

import com.alibaba.testable.core.model.LogLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OmniConstructorTest {

    @Test
    public void should_keep_origin_value() {
        DemoParent demoParent = OmniConstructor.newInstance(DemoParent.class);
        assertEquals(2, demoParent.c.gc.getStatic());
        assertEquals(1, demoParent.c.gc.get());
    }

    @Test
    public void should_handle_basic_types() {
        assertEquals(0, OmniConstructor.newInstance(int.class));
        assertEquals(0L, OmniConstructor.newInstance(Long.class));
        assertEquals("", OmniConstructor.newInstance(String.class));
        assertEquals(LogLevel.DISABLE, OmniConstructor.newInstance(LogLevel.class));
    }

}
