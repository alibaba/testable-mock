package com.alibaba.testable.demo;

import com.alibaba.testable.demo.model.BlackBox;
import com.alibaba.testable.core.annotation.MockConstructor;
import com.alibaba.testable.core.annotation.MockMethod;


import org.junit.Test;

import java.util.concurrent.Executors;

import static com.alibaba.testable.core.matcher.InvokeVerifier.verify;
import static com.alibaba.testable.core.tool.TestableTool.MOCK_CONTEXT;
import static com.alibaba.testable.core.tool.TestableTool.SOURCE_METHOD;
import static org.junit.Assert.assertEquals;

/**
 * 演示基本的Mock功能
 * Demonstrate basic mock functionality
 */
public class DemoBasicTest {

    private DemoBasic demoBasic = new DemoBasic();

    public static class Mock {
        @MockConstructor
        private BlackBox createBlackBox(String text) {
            return new BlackBox("mock_" + text);
        }

        @MockMethod(targetClass = DemoBasic.class)
        private String innerFunc(String text) {
            return "mock_" + text;
        }

        @MockMethod(targetClass = DemoBasic.class)
        private String staticFunc() {
            return "_MOCK_TAIL";
        }

        @MockMethod(targetClass = String.class)
        private String trim() {
            return "trim_string";
        }

        @MockMethod(targetClass = String.class, targetMethod = "substring")
        private String sub(int i, int j) {
            return "sub_string";
        }

        @MockMethod(targetClass = String.class)
        private boolean startsWith(String s) {
            return false;
        }

        @MockMethod(targetClass = BlackBox.class)
        private BlackBox secretBox() {
            return new BlackBox("not_secret_box");
        }

        @MockMethod(targetClass = DemoBasic.class)
        private String callFromDifferentMethod() {
            if ("special_case".equals(MOCK_CONTEXT.get("case"))) {
                return "mock_special";
            }
            switch (SOURCE_METHOD) {
                case "callerOne":
                    return "mock_one";
                default:
                    return "mock_others";
            }
        }
    }

    @Test
    public void should_mock_new_object() {
        assertEquals("mock_something", demoBasic.newFunc());
        verify("createBlackBox").with("something");
    }

    @Test
    public void should_mock_member_method() throws Exception {
        assertEquals("{ \"res\": \"mock_hello_MOCK_TAIL\"}", demoBasic.outerFunc("hello"));
        verify("innerFunc").with("hello");
        verify("staticFunc").with();
    }

    @Test
    public void should_mock_common_method() {
        assertEquals("trim_string__sub_string__false", demoBasic.commonFunc());
        verify("trim").withTimes(1);
        verify("sub").withTimes(1);
        verify("startsWith").withTimes(1);
    }

    @Test
    public void should_mock_static_method() {
        assertEquals("not_secret_box", demoBasic.getBox().get());
        verify("secretBox").withTimes(1);
    }

    @Test
    public void should_get_source_method_name() throws Exception {
        // synchronous
        assertEquals("mock_one_mock_others", demoBasic.callerOne() + "_" + demoBasic.callerTwo());
        // asynchronous
        assertEquals("mock_one_mock_others",
            Executors.newSingleThreadExecutor().submit(() -> demoBasic.callerOne() + "_" + demoBasic.callerTwo()).get());
        verify("callFromDifferentMethod").withTimes(4);
    }

    @Test
    public void should_set_mock_context() throws Exception {
        MOCK_CONTEXT.put("case", "special_case");
        // synchronous
        assertEquals("mock_special", demoBasic.callerOne());
        // asynchronous
        assertEquals("mock_special", Executors.newSingleThreadExecutor().submit(() -> demoBasic.callerOne()).get());
        verify("callFromDifferentMethod").withTimes(2);
    }

}
