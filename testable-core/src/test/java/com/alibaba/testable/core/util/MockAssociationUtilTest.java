package com.alibaba.testable.core.util;

import com.alibaba.testable.core.tool.PrivateAccessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MockAssociationUtilTest {

    @Test
    void should_associate_by_inner_mock_class() {
        assertTrue((Boolean)PrivateAccessor.invokeStatic(MockAssociationUtil.class, "isAssociatedByInnerMockClass",
            "com.alibaba.testable.DemoTest", "com.alibaba.testable.DemoTest$Mock"));
        assertFalse((Boolean)PrivateAccessor.invokeStatic(MockAssociationUtil.class, "isAssociatedByInnerMockClass",
            "com.alibaba.testable.DemoTest", "com.alibaba.testable.DemoTestMock"));
    }

    @Test
    void should_associate_by_outer_mock_class() {
        assertTrue((Boolean)PrivateAccessor.invokeStatic(MockAssociationUtil.class, "isAssociatedByOuterMockClass",
            "com.alibaba.testable.DemoTest", "com.alibaba.testable.DemoMock"));
        assertFalse((Boolean)PrivateAccessor.invokeStatic(MockAssociationUtil.class, "isAssociatedByOuterMockClass",
            "com.alibaba.testable.DemoTester", "com.alibaba.testable.DemoMock"));
    }

}
