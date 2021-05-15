package com.alibaba.demo.basic

import com.alibaba.testable.core.annotation.MockMethod
import com.alibaba.testable.core.matcher.InvokeVerifier
import com.alibaba.demo.basic.model.mock.BlackBox
import com.alibaba.demo.basic.model.mock.Box
import com.alibaba.demo.basic.model.mock.Color
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * 演示父类变量引用子类对象时的Mock场景
 * Demonstrate scenario of mocking method from sub-type object referred by parent-type variable
 */
internal class DemoInheritTest {

    private val demoInherit = DemoInherit()

    class Mock {
        @MockMethod(targetMethod = "put")
        private fun put_into_box(self: Box, something: String) {
            self.put("put_" + something + "_into_box")
        }

        @MockMethod(targetMethod = "put")
        private fun put_into_blackbox(self: BlackBox, something: String) {
            self.put("put_" + something + "_into_blackbox")
        }

        @MockMethod(targetMethod = "get")
        private fun get_from_box(self: Box): String {
            return "get_from_box"
        }

        @MockMethod(targetMethod = "get")
        private fun get_from_blackbox(self: BlackBox): String {
            return "get_from_blackbox"
        }

        @MockMethod(targetMethod = "getColor")
        private fun get_color_from_color(self: Color): String {
            return "color_from_color"
        }

        @MockMethod(targetMethod = "getColor")
        private fun get_color_from_blackbox(self: BlackBox): String {
            return "color_from_blackbox"
        }
    }

    @Test
    fun should_mock_call_sub_object_method_by_parent_object() {
        val box = demoInherit.putIntoBox() as BlackBox
        InvokeVerifier.verify("put_into_box").withTimes(1)
        assertEquals("put_data_into_box", box.get())
    }

    @Test
    fun should_mock_call_sub_object_method_by_sub_object() {
        val box = demoInherit.putIntoBlackBox()
        InvokeVerifier.verify("put_into_blackbox").withTimes(1)
        assertEquals("put_data_into_blackbox", box.get())
    }

    @Test
    fun should_mock_call_parent_object_method_by_parent_object() {
        val content = demoInherit.fromBox
        InvokeVerifier.verify("get_from_box").withTimes(1)
        assertEquals("get_from_box", content)
    }

    @Test
    fun should_mock_call_parent_object_method_by_sub_object() {
        val content = demoInherit.fromBlackBox
        InvokeVerifier.verify("get_from_blackbox").withTimes(1)
        assertEquals("get_from_blackbox", content)
    }

    @Test
    fun should_mock_call_interface_method_by_interface_object() {
        val color = demoInherit.colorViaColor
        InvokeVerifier.verify("get_color_from_color").withTimes(1)
        assertEquals("color_from_color", color)
    }

    @Test
    fun should_mock_call_interface_method_by_sub_class_object() {
        val color = demoInherit.colorViaBox
        InvokeVerifier.verify("get_color_from_blackbox").withTimes(1)
        assertEquals("color_from_blackbox", color)
    }
}
