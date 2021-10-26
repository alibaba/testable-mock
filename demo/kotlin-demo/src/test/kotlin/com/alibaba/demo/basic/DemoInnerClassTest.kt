package com.alibaba.demo.basic

import com.alibaba.testable.core.annotation.MockMethod
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * 演示对内部类的Mock支持
 * Demonstrate support for mocking invocation inside a inner class
 */
internal class DemoInnerClassTest {

    class Mock {
        @MockMethod(targetClass = DemoInnerClass::class)
        fun methodToBeMock(): String {
            return "MockedCall"
        }
    }

    @Test
    @Throws(Exception::class)
    fun should_mock_invoke_inside_inner_class() {
        val demo = DemoInnerClass()
        assertEquals("MockedCall", demo.callInnerDemo())
        assertEquals("MockedCall", demo.callAnonymousInner())
        assertEquals("MockedCall", DemoInnerClass.StaticInner().demo())
    }
}
