package com.alibaba.demo.basic;

import com.alibaba.demo.basic.model.mock.BlackBox;
import com.alibaba.testable.core.annotation.MockNew;
import com.alibaba.testable.core.annotation.MockInvoke;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;

import static com.alibaba.testable.core.matcher.InvocationVerifier.verifyInvoked;
import static com.alibaba.testable.core.tool.TestableTool.MOCK_CONTEXT;
import static com.alibaba.testable.core.tool.TestableTool.SOURCE_METHOD;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 演示基本的Mock功能
 * Demonstrate basic mock functionality
 */
class DemoMockTest {

    private DemoMock demoMock = new DemoMock();

    public static class Mock {
        @MockNew
        private BlackBox createBlackBox(String text) {
            return new BlackBox("mock_" + text);
        }

        @MockInvoke(targetClass = DemoMock.class)
        private String innerFunc(String text) {
            return "mock_" + text;
        }

        @MockInvoke(targetClass = DemoMock.class)
        private String staticFunc() {
            return "_MOCK_TAIL";
        }

        @MockInvoke(targetClass = String.class)
        private String trim() {
            return "trim_string";
        }

        @MockInvoke(targetClass = String.class, targetMethod = "substring")
        private String sub(int i, int j) {
            return "sub_string";
        }

        @MockInvoke(targetClass = String.class)
        private boolean startsWith(String s) {
            return false;
        }

        @MockInvoke(targetClass = BlackBox.class)
        private BlackBox secretBox() {
            return new BlackBox("not_secret_box");
        }

        @MockInvoke(targetClass = DemoMock.class)
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
    void should_mock_new_object() {
        assertEquals("mock_something", demoMock.newFunc());
        verifyInvoked("createBlackBox").with("something");
    }

    @Test
    void should_mock_member_method() throws Exception {
        assertEquals("{ \"res\": \"mock_hello_MOCK_TAIL\"}", demoMock.outerFunc("hello"));
        verifyInvoked("innerFunc").with("hello");
        verifyInvoked("staticFunc").with();
    }

    @Test
    void should_mock_common_method() {
        assertEquals("trim_string__sub_string__false", demoMock.commonFunc());
        verifyInvoked("trim").withTimes(1);
        verifyInvoked("sub").withTimes(1);
        verifyInvoked("startsWith").withTimes(1);
    }

    @Test
    void should_mock_static_method() {
        assertEquals("not_secret_box", demoMock.getBox().get());
        verifyInvoked("secretBox").withTimes(1);
    }

    @Test
    void should_get_source_method_name() throws Exception {
        // synchronous
        assertEquals("mock_one_mock_others", demoMock.callerOne() + "_" + demoMock.callerTwo());
        // asynchronous
        assertEquals("mock_one_mock_others",
            Executors.newSingleThreadExecutor().submit(() -> demoMock.callerOne() + "_" + demoMock.callerTwo()).get());
        verifyInvoked("callFromDifferentMethod").withTimes(4);
    }

    @Test
    void should_set_mock_context() throws Exception {
        MOCK_CONTEXT.put("case", "special_case");
        // synchronous
        assertEquals("mock_special", demoMock.callerOne());
        // asynchronous
        assertEquals("mock_special", Executors.newSingleThreadExecutor().submit(() -> demoMock.callerOne()).get());
        verifyInvoked("callFromDifferentMethod").withTimes(2);
    }

}
