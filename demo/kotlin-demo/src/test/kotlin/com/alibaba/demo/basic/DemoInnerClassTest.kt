package com.alibaba.demo.basic

import com.alibaba.testable.core.annotation.MockMethod
import org.junit.jupiter.api.Assertions
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
        Assertions.assertEquals("MockedCall", demo.callInnerDemo())
        Assertions.assertEquals("MockedCall", demo.callAnonymousInner())
        Assertions.assertEquals("MockedCall", DemoInnerClass.StaticInner().demo())
    }
}
