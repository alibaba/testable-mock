package com.alibaba.testable.demo

import com.alibaba.testable.core.accessor.PrivateAccessor
import com.alibaba.testable.core.annotation.TestableMock
import com.alibaba.testable.core.tool.TestableTool.*
import com.alibaba.testable.processor.annotation.EnablePrivateAccess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors


@EnablePrivateAccess
internal class DemoServiceTest {

    @TestableMock(targetMethod = CONSTRUCTOR)
    private fun createBlackBox(text: String) = BlackBox("mock_$text")

    @TestableMock
    private fun innerFunc(self: DemoService, text: String) = "mock_$text"

    @TestableMock
    private fun trim(self: BlackBox) = "trim_string"

    @TestableMock(targetMethod = "substring")
    private fun sub(self: BlackBox, i: Int, j: Int) = "sub_string"

    @TestableMock
    private fun startsWith(self: BlackBox, s: String) = false

    @TestableMock
    private fun secretBox(ignore: BlackBox): BlackBox {
        return BlackBox("not_secret_box")
    }

    @TestableMock
    private fun createBox(ignore: ColorBox, color: String, box: BlackBox): BlackBox {
        return BlackBox("White_${box.callMe()}")
    }

    @TestableMock
    private fun callFromDifferentMethod(self: DemoService): String {
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
    fun should_able_to_mock_private_method() {
        assertEquals("hello - 1", PrivateAccessor.invoke(demoService, "privateFunc", "hello", 1))
    }

    @Test
    fun should_able_to_mock_private_field() {
        PrivateAccessor.set(demoService, "count", 3)
        assertEquals("5", demoService.privateFieldAccessFunc())
        assertEquals(5, PrivateAccessor.get(demoService, "count"))
    }

    @Test
    fun should_able_to_mock_new_object() {
        assertEquals("mock_something", demoService.newFunc())
        verify("createBlackBox").times(1)
    }

    @Test
    fun should_able_to_mock_member_method() {
        assertEquals("{ \"res\": \"mock_hello\"}", demoService.outerFunc("hello"))
        verify("innerFunc").times(1)
    }

    @Test
    fun should_able_to_mock_common_method() {
        assertEquals("trim_string__sub_string__false", demoService.commonFunc())
        verify("trim").times(1)
        verify("sub").times(1)
        verify("startsWith").times(1)
    }

    @Test
    fun should_able_to_mock_static_method() {
        assertEquals("White_not_secret_box", demoService.getBox().callMe())
        verify("secretBox").times(1)
        verify("createBox").times(1)
    }

    @Test
    fun should_able_to_get_source_method_name() {
        // synchronous
        assertEquals("mock_one_mock_others", demoService.callerOne() + "_" + demoService.callerTwo())
        // asynchronous
        assertEquals("mock_one_mock_others", Executors.newSingleThreadExecutor().submit<String> {
            demoService.callerOne() + "_" + demoService.callerTwo()
        }.get())
        verify("callFromDifferentMethod").times(4)
    }

    @Test
    fun should_able_to_get_test_case_name() {
        // synchronous
        assertEquals("mock_special", demoService.callerOne())
        // asynchronous
        assertEquals("mock_special", Executors.newSingleThreadExecutor().submit<String> {
            demoService.callerOne()
        }.get())
        verify("callFromDifferentMethod").times(2)
    }
}
