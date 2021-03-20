package com.alibaba.testable.core.tool;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OmniConstructorTest {

    @Test
    public void should_keep_origin_value() {
        DemoParent demoParent = OmniConstructor.newInstance(DemoParent.class);
        assertEquals(2, demoParent.c.gc.getStatic());
        assertEquals(1, demoParent.c.gc.get());
    }

}
