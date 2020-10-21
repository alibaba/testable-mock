package com.alibaba.testable.demo;

import com.alibaba.testable.core.accessor.PrivateAccessor;
import com.alibaba.testable.core.annotation.EnableTestable;
import com.alibaba.testable.core.annotation.TestableInject;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static com.alibaba.testable.core.tool.TestableTool.SOURCE_METHOD;
import static com.alibaba.testable.core.tool.TestableTool.TEST_CASE;
import static org.junit.jupiter.api.Assertions.assertEquals;

@EnableTestable
class DemoServiceTest {

    @TestableInject
    private BlackBox createBlackBox(String text) {
        return new BlackBox("mock_" + text);
    }

    @TestableInject
    private String innerFunc(String text) {
        return "mock_" + text;
    }

    @TestableInject(targetClass="java.lang.String")
    private String trim(String self) {
        return "trim_string";
    }

    @TestableInject(targetClass="java.lang.String", targetMethod = "substring")
    private String sub(String self, int i, int j) {
        return "sub_string";
    }

    @TestableInject(targetClass="java.lang.String")
    private boolean startsWith(String self, String s) {
        return false;
    }

    @TestableInject
    private String callFromDifferentMethod() {
        if (TEST_CASE.equals("should_able_to_get_test_case_name")) {
            return "mock_special";
        }
        switch (SOURCE_METHOD) {
            case "callerOne": return "mock_one";
            default: return "mock_others";
        }
    }

    private DemoService demoService = new DemoService();

    @Test
    void should_able_to_test_private_method() throws Exception {
        assertEquals("hello - 1", demoService.privateFunc("hello", 1));
        assertEquals("hello - 1", PrivateAccessor.invoke(demoService, "privateFunc", "hello", 1));
    }

    @Test
    void should_able_to_test_private_field() throws Exception {
        demoService.count = 2;
        assertEquals("4", demoService.privateFieldAccessFunc());
        PrivateAccessor.set(demoService, "count", 3);
        assertEquals("5", demoService.privateFieldAccessFunc());
        assertEquals(new Integer(5), PrivateAccessor.get(demoService, "count"));
    }

    @Test
    void should_able_to_test_new_object() throws Exception {
        assertEquals("mock_something", demoService.newFunc());
    }

    @Test
    void should_able_to_test_member_method() throws Exception {
        assertEquals("{ \"res\": \"mock_hello\"}", demoService.outerFunc("hello"));
    }

    @Test
    void should_able_to_test_common_method() throws Exception {
        assertEquals("trim_string__sub_string__false", demoService.commonFunc());
    }

    @Test
    void should_able_to_get_source_method_name() throws Exception {
        // synchronous
        assertEquals("mock_one_mock_others", demoService.callerOne() + "_" + demoService.callerTwo());
        // asynchronous
        assertEquals("mock_one_mock_others",
            Executors.newSingleThreadExecutor().submit(() -> demoService.callerOne() + "_" + demoService.callerTwo()).get());
    }

    @Test
    void should_able_to_get_test_case_name() throws Exception {
        // synchronous
        assertEquals("mock_special", demoService.callerOne());
        // asynchronous
        assertEquals("mock_special", Executors.newSingleThreadExecutor().submit(() -> demoService.callerOne()).get());
    }

}
