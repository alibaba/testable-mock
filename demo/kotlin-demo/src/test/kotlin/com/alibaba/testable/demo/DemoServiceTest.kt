package com.alibaba.testable.demo

import com.alibaba.testable.core.accessor.PrivateAccessor
import com.alibaba.testable.core.annotation.EnableTestable
import com.alibaba.testable.core.annotation.TestableInject
import com.alibaba.testable.core.tool.TestableTool.SOURCE_METHOD
import com.alibaba.testable.core.tool.TestableTool.TEST_CASE
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors


@EnableTestable
internal class DemoServiceTest {

    @TestableInject
    private fun createBlackBox(text: String) = BlackBox("mock_$text")

    @TestableInject
    private fun innerFunc(text: String) = "mock_$text"

    @TestableInject(targetClass = "com.alibaba.testable.demo.BlackBox")
    private fun trim(self: BlackBox) = "trim_string"

    @TestableInject(targetClass = "com.alibaba.testable.demo.BlackBox", targetMethod = "substring")
    private fun sub(self: BlackBox, i: Int, j: Int) = "sub_string"

    @TestableInject(targetClass = "com.alibaba.testable.demo.BlackBox")
    private fun startsWith(self: BlackBox, s: String) = false

    @TestableInject
    private fun callFromDifferentMethod(): String {
        return if (TEST_CASE == "should_able_to_get_test_case_name") {
            "mock_special"
        } else {
            when (SOURCE_METHOD) {
                "callerOne" -> "mock_one"
                else -> "mock_others"
            }
        }
    }

    private val demoService = DemoService()

    @Test
    fun should_able_to_test_private_method() {
        assertEquals("hello - 1", PrivateAccessor.invoke(demoService, "privateFunc", "hello", 1))
    }

    @Test
    fun should_able_to_test_private_field() {
        PrivateAccessor.set(demoService, "count", 3)
        assertEquals("5", demoService.privateFieldAccessFunc())
        assertEquals(5, PrivateAccessor.get(demoService, "count"))
    }

    @Test
    fun should_able_to_test_new_object() {
        assertEquals("mock_something", demoService.newFunc())
    }

    @Test
    fun should_able_to_test_member_method() {
        assertEquals("{ \"res\": \"mock_hello\"}", demoService.outerFunc("hello"))
    }

    @Test
    fun should_able_to_test_common_method() {
        assertEquals("trim_string__sub_string__false", demoService.commonFunc())
    }

    @Test
    fun should_able_to_get_source_method_name() {
        // synchronous
        assertEquals("mock_one_mock_others", demoService.callerOne() + "_" + demoService.callerTwo())
        // asynchronous
        assertEquals("mock_one_mock_others", Executors.newSingleThreadExecutor().submit<String> {
            demoService.callerOne() + "_" + demoService.callerTwo()
        }.get())
    }

    @Test
    fun should_able_to_get_test_case_name() {
        // synchronous
        assertEquals("mock_special", demoService.callerOne())
        // asynchronous
        assertEquals("mock_special", Executors.newSingleThreadExecutor().submit<String> {
            demoService.callerOne()
        }.get())
    }
}
