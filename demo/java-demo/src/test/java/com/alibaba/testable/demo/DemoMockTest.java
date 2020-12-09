package com.alibaba.testable.demo;

import com.alibaba.testable.core.annotation.TestableMock;
import com.alibaba.testable.demo.model.BlackBox;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.Executors;

import static com.alibaba.testable.core.matcher.InvokeVerifier.verify;
import static com.alibaba.testable.core.tool.TestableTool.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 演示基本的Mock功能
 * Demonstrate basic mock functionality
 */
class DemoMockTest {

    private DemoMock demoMock = new DemoMock();

    @TestableMock(targetMethod = CONSTRUCTOR)
    private BlackBox createBlackBox(String text) {
        return new BlackBox("mock_" + text);
    }

    @TestableMock
    private String innerFunc(DemoMock self, String text) {
        return "mock_" + text;
    }

    @TestableMock
    private String trim(String self) {
        return "trim_string";
    }

    @TestableMock(targetMethod = "substring")
    private String sub(String self, int i, int j) {
        return "sub_string";
    }

    @TestableMock
    private boolean startsWith(String self, String s) {
        return false;
    }

    @TestableMock
    private BlackBox secretBox(BlackBox ignore) {
        return new BlackBox("not_secret_box");
    }

    @TestableMock
    private String callFromDifferentMethod(DemoMock self) {
        if (TEST_CASE.equals("should_able_to_get_test_case_name")) {
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
        assertEquals("{ \"res\": \"mock_hello\"}", demoMock.outerFunc("hello"));
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
        // synchronous
        assertEquals("mock_special", demoMock.callerOne());
        // asynchronous
        assertEquals("mock_special", Executors.newSingleThreadExecutor().submit(() -> demoMock.callerOne()).get());
        verify("callFromDifferentMethod").withTimes(2);
    }

}
