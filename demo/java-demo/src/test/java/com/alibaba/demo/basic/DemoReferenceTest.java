package com.alibaba.demo.basic;

import com.alibaba.demo.basic.model.mock.BlackBox;
import com.alibaba.demo.basic.model.mock.Box;
import com.alibaba.demo.basic.model.mock.Color;
import com.alibaba.testable.core.annotation.MockInvoke;
import org.junit.jupiter.api.Test;

import static com.alibaba.testable.core.matcher.InvocationVerifier.verifyInvoked;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 演示父类变量引用子类对象时的Mock场景
 * Demonstrate scenario of mocking method from sub-type object referred by parent-type variable
 */
class DemoInheritTest {

    private DemoInherit demoInherit = new DemoInherit();

    public static class Mock {

        /**
         * 当需要引用原对象时，不加`targetClass`参数，将原类型作为Mock方法的首个参数，建议将参数命名为`self`
         * When method body need to refer the invoked object, skip the use `targetClass` parameter,
         * and add a parameter `self` with target class type before other paramter of the target method
         */
        @MockInvoke(targetMethod = "put")
        private void put_into_box(Box self, String something) {
            self.put("put_" + something + "_into_box");
        }

        @MockInvoke(targetMethod = "put")
        private void put_into_blackbox(BlackBox self, String something) {
            self.put("put_" + something + "_into_blackbox");
        }

        /**
         * 当不需要引用原对象时，将目标类型作为`@MockInvoke`注解的`targetClass`参数值即可
         * When the invoked object is not used in the mock method, set `targetClass` value to the type who owns the mocked method
         */
        @MockInvoke(targetClass = Box.class, targetMethod = "get")
        private String get_from_box() {
            return "get_from_box";
        }

        @MockInvoke(targetClass = BlackBox.class, targetMethod = "get")
        private String get_from_blackbox() {
            return "get_from_blackbox";
        }

        @MockInvoke(targetClass = Color.class, targetMethod = "getColor")
        private String get_color_from_color() {
            return "color_from_color";
        }

        @MockInvoke(targetClass = BlackBox.class, targetMethod = "getColor")
        private String get_color_from_blackbox() {
        return "color_from_blackbox";
    }

        @MockInvoke(targetClass = Color.class, targetMethod = "getColorIndex")
        private String get_colorIdx_from_color() {
            return "colorIdx_from_color";
        }
    }

    @Test
    void should_mock_call_sub_object_method_by_parent_object() {
        BlackBox box = (BlackBox)demoInherit.putIntoBox();
        verifyInvoked("put_into_box").withTimes(1);
        assertEquals("put_data_into_box", box.get());
    }

    @Test
    void should_mock_call_sub_object_method_by_sub_object() {
        BlackBox box = demoInherit.putIntoBlackBox();
        verifyInvoked("put_into_blackbox").withTimes(1);
        assertEquals("put_data_into_blackbox", box.get());
    }

    @Test
    void should_mock_call_parent_object_method_by_parent_object() {
        String content = demoInherit.getFromBox();
        verifyInvoked("get_from_box").withTimes(1);
        assertEquals("get_from_box", content);
    }

    @Test
    void should_mock_call_parent_object_method_by_sub_object() {
        String content = demoInherit.getFromBlackBox();
        verifyInvoked("get_from_blackbox").withTimes(1);
        assertEquals("get_from_blackbox", content);
    }

    @Test
    void should_mock_call_interface_method_by_interface_object() {
        String color = demoInherit.getColorViaColor();
        verifyInvoked("get_color_from_color").withTimes(1);
        assertEquals("color_from_color", color);
    }

    @Test
    void should_mock_call_interface_method_by_sub_class_object() {
        String color = demoInherit.getColorViaBox();
        verifyInvoked("get_color_from_blackbox").withTimes(1);
        assertEquals("color_from_blackbox", color);
    }

    @Test
    void should_mock_call_interface_method_by_sub_interface_object() {
        String colorIdx = demoInherit.getColorIdxViaColor();
        verifyInvoked("get_colorIdx_from_color").withTimes(1);
        assertEquals("colorIdx_from_color", colorIdx);
    }
}
