package com.alibaba.testable.demo

import com.alibaba.testable.core.annotation.TestableMock
import com.alibaba.testable.core.matcher.InvokeVerifier
import com.alibaba.testable.demo.model.BlackBox
import com.alibaba.testable.demo.model.Box
import com.alibaba.testable.demo.model.Color
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class DemoInheritTest {

    @TestableMock(targetMethod = "put")
    private fun put_into_box(self: Box, something: String) {
        self.put("put_" + something + "_into_box")
    }

    @TestableMock(targetMethod = "put")
    private fun put_into_blackbox(self: BlackBox, something: String) {
        self.put("put_" + something + "_into_blackbox")
    }

    @TestableMock(targetMethod = "get")
    private fun get_from_box(self: Box): String {
        return "get_from_box"
    }

    @TestableMock(targetMethod = "get")
    private fun get_from_blackbox(self: BlackBox): String {
        return "get_from_blackbox"
    }

    @TestableMock(targetMethod = "getColor")
    private fun get_color_from_color(self: Color): String {
        return "color_from_color"
    }

    @TestableMock(targetMethod = "getColor")
    private fun get_color_from_blackbox(self: BlackBox): String {
        return "color_from_blackbox"
    }

    private val demoInherit = DemoInherit()

    @Test
    fun should_able_to_mock_call_sub_object_method_by_parent_object() {
        val box = demoInherit.putIntoBox() as BlackBox
        InvokeVerifier.verify("put_into_box").withTimes(1)
        Assertions.assertEquals("put_data_into_box", box.get())
    }

    @Test
    fun should_able_to_mock_call_sub_object_method_by_sub_object() {
        val box = demoInherit.putIntoBlackBox()
        InvokeVerifier.verify("put_into_blackbox").withTimes(1)
        Assertions.assertEquals("put_data_into_blackbox", box.get())
    }

    @Test
    fun should_able_to_mock_call_parent_object_method_by_parent_object() {
        val content = demoInherit.fromBox
        InvokeVerifier.verify("get_from_box").withTimes(1)
        Assertions.assertEquals("get_from_box", content)
    }

    @Test
    fun should_able_to_mock_call_parent_object_method_by_sub_object() {
        val content = demoInherit.fromBlackBox
        InvokeVerifier.verify("get_from_blackbox").withTimes(1)
        Assertions.assertEquals("get_from_blackbox", content)
    }

    @Test
    fun should_able_to_mock_call_interface_method_by_interface_object() {
        val color = demoInherit.colorViaColor
        InvokeVerifier.verify("get_color_from_color").withTimes(1)
        Assertions.assertEquals("color_from_color", color)
    }

    @Test
    fun should_able_to_mock_call_interface_method_by_sub_class_object() {
        val color = demoInherit.colorViaBox
        InvokeVerifier.verify("get_color_from_blackbox").withTimes(1)
        Assertions.assertEquals("color_from_blackbox", color)
    }
}
