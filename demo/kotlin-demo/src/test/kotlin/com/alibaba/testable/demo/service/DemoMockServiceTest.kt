package com.alibaba.testable.demo.service

import com.alibaba.testable.core.annotation.TestableMock
import com.alibaba.testable.core.tool.TestableTool.*
import com.alibaba.testable.demo.model.BlackBox
import com.alibaba.testable.demo.model.Box
import com.alibaba.testable.demo.model.ColorBox
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors


internal class DemoMockServiceTest {

    @TestableMock(targetMethod = CONSTRUCTOR)
    private fun createBlackBox(text: String) = BlackBox("mock_$text")

    @TestableMock
    private fun innerFunc(self: DemoMockService, text: String) = "mock_$text"

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
        return BlackBox("White_${box.get()}")
    }

    @TestableMock
    private fun put(self: Box, something: String) {
        self.put("put_" + something + "_mocked")
    }

    @TestableMock
    private fun callFromDifferentMethod(self: DemoMockService): String {
        return if (TEST_CASE == "should_able_to_get_test_case_name") {
            "mock_special"
        } else {
            when (SOURCE_METHOD) {
                "callerOne" -> "mock_one"
                else -> "mock_others"
            }
        }
    }

    private val demoService = DemoMockService()

    @Test
    fun should_able_to_mock_new_object() {
        assertEquals("mock_something", demoService.newFunc())
        verify("createBlackBox").with("something")
    }

    @Test
    fun should_able_to_mock_member_method() {
        assertEquals("{ \"res\": \"mock_hello\"}", demoService.outerFunc("hello"))
        verify("innerFunc").with("hello")
    }

    @Test
    fun should_able_to_mock_common_method() {
        assertEquals("trim_string__sub_string__false", demoService.commonFunc())
        verify("trim").withTimes(1)
        verify("sub").withTimes(1)
        verify("startsWith").withTimes(1)
    }

    @Test
    fun should_able_to_mock_static_method() {
        assertEquals("White_not_secret_box", demoService.getBox().get())
        verify("secretBox").withTimes(1)
        verify("createBox").withTimes(1)
    }

    @Test
    fun should_able_to_mock_override_method() {
        val box = demoService.putBox() as BlackBox
        verify("put").withTimes(1)
        assertEquals("put_data_mocked", box.get())
    }

    @Test
    fun should_able_to_get_source_method_name() {
        // synchronous
        assertEquals("mock_one_mock_others", demoService.callerOne() + "_" + demoService.callerTwo())
        // asynchronous
        assertEquals("mock_one_mock_others", Executors.newSingleThreadExecutor().submit<String> {
            demoService.callerOne() + "_" + demoService.callerTwo()
        }.get())
        verify("callFromDifferentMethod").withTimes(4)
    }

    @Test
    fun should_able_to_get_test_case_name() {
        // synchronous
        assertEquals("mock_special", demoService.callerOne())
        // asynchronous
        assertEquals("mock_special", Executors.newSingleThreadExecutor().submit<String> {
            demoService.callerOne()
        }.get())
        verify("callFromDifferentMethod").withTimes(2)
    }
}
