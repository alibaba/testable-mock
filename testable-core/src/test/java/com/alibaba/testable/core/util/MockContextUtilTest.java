package com.alibaba.testable.core.util;

import com.alibaba.testable.core.accessor.PrivateAccessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MockContextUtilTest {

    @Test
    void should_able_to_associate_by_inner_mock_class() {
        assertTrue((Boolean)PrivateAccessor.invokeStatic(MockContextUtil.class, "isAssociatedByInnerMockClass",
            "com.alibaba.testable.DemoTest", "com.alibaba.testable.DemoTest$Mock"));
        assertFalse((Boolean)PrivateAccessor.invokeStatic(MockContextUtil.class, "isAssociatedByInnerMockClass",
            "com.alibaba.testable.DemoTest", "com.alibaba.testable.DemoTestMock"));
    }

    @Test
    void should_able_to_associate_by_outer_mock_class() {
        assertTrue((Boolean)PrivateAccessor.invokeStatic(MockContextUtil.class, "isAssociatedByOuterMockClass",
            "com.alibaba.testable.DemoTest", "com.alibaba.testable.DemoMock"));
        assertFalse((Boolean)PrivateAccessor.invokeStatic(MockContextUtil.class, "isAssociatedByOuterMockClass",
            "com.alibaba.testable.DemoTester", "com.alibaba.testable.DemoMock"));
    }

}
