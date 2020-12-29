package com.alibaba.testable.demo;

import com.alibaba.testable.core.annotation.MockConstructor;
import com.alibaba.testable.core.annotation.MockMethod;
import com.alibaba.testable.demo.model.BlackBox;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;

import static com.alibaba.testable.core.matcher.InvokeVerifier.verify;
import static com.alibaba.testable.core.tool.TestableTool.SOURCE_METHOD;
import static com.alibaba.testable.core.tool.TestableTool.MOCK_CONTEXT;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 演示基本的Mock功能
 * Demonstrate basic mock functionality
 */
class DemoMockTest {

    private DemoMock demoMock = new DemoMock();

    @MockConstructor
    private BlackBox createBlackBox(String text) {
        return new BlackBox("mock_" + text);
    }

    @MockMethod
    private String innerFunc(DemoMock self, String text) {
        return "mock_" + text;
    }

    @MockMethod
    private String staticFunc(DemoMock self) {
        return "_MOCK_TAIL";
    }

    @MockMethod
    private String trim(String self) {
        return "trim_string";
    }

    @MockMethod(targetMethod = "substring")
    private String sub(String self, int i, int j) {
        return "sub_string";
    }

    @MockMethod
    private boolean startsWith(String self, String s) {
        return false;
    }

    @MockMethod
    private BlackBox secretBox(BlackBox ignore) {
        return new BlackBox("not_secret_box");
    }

    @MockMethod
    private String callFromDifferentMethod(DemoMock self) {
        if ("special_case".equals(MOCK_CONTEXT.get("case"))) {
            return "mock_special";
        }
        switch (SOURCE_METHOD) {
            case "callerOne": return "mock_one";
            default: return "mock_others";
        }
    }


    @Test
    void should_able_to_mock_new_object() {
        assertEquals("mock_something", demoMock.newFunc());
        verify("createBlackBox").with("something");
    }

    @Test
    void should_able_to_mock_member_method() throws Exception {
        assertEquals("{ \"res\": \"mock_hello_MOCK_TAIL\"}", demoMock.outerFunc("hello"));
        verify("innerFunc").with("hello");
    }

    @Test
    void should_able_to_mock_common_method() {
        assertEquals("trim_string__sub_string__false", demoMock.commonFunc());
        verify("trim").withTimes(1);
        verify("sub").withTimes(1);
        verify("startsWith").withTimes(1);
    }

    @Test
    void should_able_to_mock_static_method() {
        assertEquals("not_secret_box", demoMock.getBox().get());
        verify("secretBox").withTimes(1);
    }

    @Test
    void should_able_to_get_source_method_name() throws Exception {
        // synchronous
        assertEquals("mock_one_mock_others", demoMock.callerOne() + "_" + demoMock.callerTwo());
        // asynchronous
        assertEquals("mock_one_mock_others",
            Executors.newSingleThreadExecutor().submit(() -> demoMock.callerOne() + "_" + demoMock.callerTwo()).get());
        verify("callFromDifferentMethod").withTimes(4);
    }

    @Test
    void should_able_to_get_test_case_name() throws Exception {
        MOCK_CONTEXT.put("case", "special_case");
        // synchronous
        assertEquals("mock_special", demoMock.callerOne());
        // asynchronous
        assertEquals("mock_special", Executors.newSingleThreadExecutor().submit(() -> demoMock.callerOne()).get());
        verify("callFromDifferentMethod").withTimes(2);
        MOCK_CONTEXT.clear();
    }

}
