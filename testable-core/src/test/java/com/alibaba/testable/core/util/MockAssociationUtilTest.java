package com.alibaba.testable.core.util;

import org.junit.jupiter.api.Test;

import static com.alibaba.testable.core.tool.PrivateAccessor.invokeStatic;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockAssociationUtilTest {

    @Test
    void should_associate_by_inner_mock_class() {
        assertTrue((Boolean)invokeStatic(MockAssociationUtil.class, "isAssociatedByInnerMockClass",
            "com.alibaba.testable.DemoTest", "com.alibaba.testable.DemoTest$Mock"));
        assertFalse((Boolean)invokeStatic(MockAssociationUtil.class, "isAssociatedByInnerMockClass",
            "com.alibaba.testable.DemoTest", "com.alibaba.testable.DemoTestMock"));
    }

    @Test
    void should_associate_by_outer_mock_class() {
        assertTrue((Boolean)invokeStatic(MockAssociationUtil.class, "isAssociatedByOuterMockClass",
            "com.alibaba.testable.DemoTest", "com.alibaba.testable.DemoMock"));
        assertFalse((Boolean)invokeStatic(MockAssociationUtil.class, "isAssociatedByOuterMockClass",
            "com.alibaba.testable.DemoTester", "com.alibaba.testable.DemoMock"));
    }

}
