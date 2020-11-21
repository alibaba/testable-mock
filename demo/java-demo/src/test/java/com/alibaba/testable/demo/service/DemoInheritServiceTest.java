package com.alibaba.testable.demo.service;

import com.alibaba.testable.core.annotation.TestableMock;
import com.alibaba.testable.demo.model.BlackBox;
import com.alibaba.testable.demo.model.Box;
import com.alibaba.testable.demo.model.Color;
import org.junit.jupiter.api.Test;

import static com.alibaba.testable.core.matcher.InvokeVerifier.verify;
import static org.junit.jupiter.api.Assertions.*;

class DemoInheritServiceTest {

    @TestableMock(targetMethod = "put")
    private void put_into_box(Box self, String something) {
        self.put("put_" + something + "_into_box");
    }

    @TestableMock(targetMethod = "put")
    private void put_into_blackbox(BlackBox self, String something) {
        self.put("put_" + something + "_into_blackbox");
    }

    @TestableMock(targetMethod = "get")
    private String get_from_box(Box self) {
        return "get_from_box";
    }

    @TestableMock(targetMethod = "get")
    private String get_from_blackbox(BlackBox self) {
        return "get_from_blackbox";
    }

    @TestableMock(targetMethod = "getColor")
    private String get_color_from_color(Color self) {
        return "color_from_color";
    }

    @TestableMock(targetMethod = "getColor")
    private String get_color_from_blackbox(BlackBox self) {
        return "color_from_blackbox";
    }

    private DemoInheritService inheritService = new DemoInheritService();

    @Test
    void should_able_to_mock_call_sub_object_method_by_parent_object() throws Exception {
        BlackBox box = (BlackBox)inheritService.putIntoBox();
        verify("put_into_box").withTimes(1);
        assertEquals("put_data_into_box", box.get());
    }

    @Test
    void should_able_to_mock_call_sub_object_method_by_sub_object() throws Exception {
        BlackBox box = inheritService.putIntoBlackBox();
        verify("put_into_blackbox").withTimes(1);
        assertEquals("put_data_into_blackbox", box.get());
    }

    @Test
    void should_able_to_mock_call_parent_object_method_by_parent_object() throws Exception {
        String content = inheritService.getFromBox();
        verify("get_from_box").withTimes(1);
        assertEquals("get_from_box", content);
    }

    @Test
    void should_able_to_mock_call_parent_object_method_by_sub_object() throws Exception {
        String content = inheritService.getFromBlackBox();
        verify("get_from_blackbox").withTimes(1);
        assertEquals("get_from_blackbox", content);
    }

    @Test
    void should_able_to_mock_call_interface_method_by_interface_object() throws Exception {
        String color = inheritService.getColorViaColor();
        verify("get_color_from_color").withTimes(1);
        assertEquals("color_from_color", color);
    }

    @Test
    void should_able_to_mock_call_interface_method_by_sub_class_object() throws Exception {
        String color = inheritService.getColorViaBox();
        verify("get_color_from_blackbox").withTimes(1);
        assertEquals("color_from_blackbox", color);
    }

}
