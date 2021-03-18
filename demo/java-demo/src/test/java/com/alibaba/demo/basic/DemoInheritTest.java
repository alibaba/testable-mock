package com.alibaba.demo.basic;

import com.alibaba.demo.basic.model.mock.BlackBox;
import com.alibaba.demo.basic.model.mock.Box;
import com.alibaba.demo.basic.model.mock.Color;
import com.alibaba.testable.core.annotation.MockMethod;
import org.junit.jupiter.api.Test;

import static com.alibaba.testable.core.matcher.InvokeVerifier.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 演示父类变量引用子类对象时的Mock场景
 * Demonstrate scenario of mocking method from sub-type object referred by parent-type variable
 */
class DemoInheritTest {

    private DemoInherit demoInherit = new DemoInherit();

    public static class Mock {
        @MockMethod(targetMethod = "put")
        private void put_into_box(Box self, String something) {
            self.put("put_" + something + "_into_box");
        }

        @MockMethod(targetMethod = "put")
        private void put_into_blackbox(BlackBox self, String something) {
            self.put("put_" + something + "_into_blackbox");
        }

        @MockMethod(targetMethod = "get")
        private String get_from_box(Box self) {
            return "get_from_box";
        }

        @MockMethod(targetMethod = "get")
        private String get_from_blackbox(BlackBox self) {
            return "get_from_blackbox";
        }

        @MockMethod(targetMethod = "getColor")
        private String get_color_from_color(Color self) {
            return "color_from_color";
        }

        @MockMethod(targetMethod = "getColor")
        private String get_color_from_blackbox(BlackBox self) {
        return "color_from_blackbox";
    }
    }

    @Test
    void should_mock_call_sub_object_method_by_parent_object() {
        BlackBox box = (BlackBox)demoInherit.putIntoBox();
        verify("put_into_box").withTimes(1);
        assertEquals("put_data_into_box", box.get());
    }

    @Test
    void should_mock_call_sub_object_method_by_sub_object() {
        BlackBox box = demoInherit.putIntoBlackBox();
        verify("put_into_blackbox").withTimes(1);
        assertEquals("put_data_into_blackbox", box.get());
    }

    @Test
    void should_mock_call_parent_object_method_by_parent_object() {
        String content = demoInherit.getFromBox();
        verify("get_from_box").withTimes(1);
        assertEquals("get_from_box", content);
    }

    @Test
    void should_mock_call_parent_object_method_by_sub_object() {
        String content = demoInherit.getFromBlackBox();
        verify("get_from_blackbox").withTimes(1);
        assertEquals("get_from_blackbox", content);
    }

    @Test
    void should_mock_call_interface_method_by_interface_object() {
        String color = demoInherit.getColorViaColor();
        verify("get_color_from_color").withTimes(1);
        assertEquals("color_from_color", color);
    }

    @Test
    void should_mock_call_interface_method_by_sub_class_object() {
        String color = demoInherit.getColorViaBox();
        verify("get_color_from_blackbox").withTimes(1);
        assertEquals("color_from_blackbox", color);
    }

}
