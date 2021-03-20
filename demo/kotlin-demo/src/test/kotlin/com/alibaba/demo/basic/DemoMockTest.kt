package com.alibaba.demo.basic

import com.alibaba.testable.core.annotation.MockConstructor
import com.alibaba.testable.core.annotation.MockMethod
import com.alibaba.testable.core.matcher.InvokeVerifier.verify
import com.alibaba.testable.core.tool.TestableTool.SOURCE_METHOD
import com.alibaba.testable.core.tool.TestableTool.MOCK_CONTEXT
import com.alibaba.demo.basic.model.mock.BlackBox
import com.alibaba.demo.basic.model.mock.ColorBox
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors

/**
 * 演示基本的Mock功能
 * Demonstrate basic mock functionality
 */
internal class DemoMockTest {

    private val demoMock = DemoMock()

    class Mock {
        @MockConstructor
        private fun createBlackBox(text: String) = BlackBox("mock_$text")

        @MockMethod(targetClass = DemoMock::class)
        private fun innerFunc(text: String) = "mock_$text"

        @MockMethod(targetClass = DemoMock::class)
        private fun staticFunc(): String {
            return "_MOCK_TAIL";
        }

        @MockMethod(targetClass = BlackBox::class)
        private fun trim() = "trim_string"

        @MockMethod(targetClass = BlackBox::class, targetMethod = "substring")
        private fun sub(i: Int, j: Int) = "sub_string"

        @MockMethod(targetClass = BlackBox::class)
        private fun startsWith(s: String) = false

        @MockMethod(targetClass = BlackBox::class)
        private fun secretBox(): BlackBox {
            return BlackBox("not_secret_box")
        }

        @MockMethod(targetClass = ColorBox::class)
        private fun createBox(color: String, box: BlackBox): BlackBox {
            return BlackBox("White_${box.get()}")
        }

        @MockMethod(targetClass = DemoMock::class)
        private fun callFromDifferentMethod(): String {
            return if (MOCK_CONTEXT["case"] == "special_case") {
                "mock_special"
            } else {
                when (SOURCE_METHOD) {
                    "callerOne" -> "mock_one"
                    else -> "mock_others"
                }
            }
        }
    }

    @Test
    fun should_mock_new_object() {
        assertEquals("mock_something", demoMock.newFunc())
        verify("createBlackBox").with("something")
    }

    @Test
    fun should_mock_member_method() {
        assertEquals("{ \"res\": \"mock_hello_MOCK_TAIL\"}", demoMock.outerFunc("hello"))
        verify("innerFunc").with("hello")
        verify("staticFunc").with()
    }

//    @Test
//    fun should_mock_method_in_companion_object() {
//        assertEquals("CALL_MOCK_TAIL", DemoMock.callStaticFunc())
//        verify("staticFunc").with()
//    }

    @Test
    fun should_mock_common_method() {
        assertEquals("trim_string__sub_string__false", demoMock.commonFunc())
        verify("trim").withTimes(1)
        verify("sub").withTimes(1)
        verify("startsWith").withTimes(1)
    }

    @Test
    fun should_mock_static_method() {
        assertEquals("White_not_secret_box", demoMock.getBox().get())
        verify("secretBox").withTimes(1)
        verify("createBox").withTimes(1)
    }

    @Test
    fun should_get_source_method_name() {
        // synchronous
        assertEquals("mock_one_mock_others", demoMock.callerOne() + "_" + demoMock.callerTwo())
        // asynchronous
        assertEquals("mock_one_mock_others", Executors.newSingleThreadExecutor().submit<String> {
            demoMock.callerOne() + "_" + demoMock.callerTwo()
        }.get())
        verify("callFromDifferentMethod").withTimes(4)
    }

    @Test
    fun should_get_test_case_name() {
        MOCK_CONTEXT["case"] = "special_case"
        // synchronous
        assertEquals("mock_special", demoMock.callerOne())
        // asynchronous
        assertEquals("mock_special", Executors.newSingleThreadExecutor().submit<String> {
            demoMock.callerOne()
        }.get())
        verify("callFromDifferentMethod").withTimes(2)
        MOCK_CONTEXT.clear()
    }
}
