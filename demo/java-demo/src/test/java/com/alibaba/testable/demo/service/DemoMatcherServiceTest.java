package com.alibaba.testable.demo.service;

import com.alibaba.testable.core.annotation.TestableMock;
import com.alibaba.testable.demo.model.BlackBox;
import org.junit.jupiter.api.Test;

import static com.alibaba.testable.core.matcher.InvokeMatcher.*;
import static com.alibaba.testable.core.matcher.InvokeVerifier.verify;

class DemoMatcherServiceTest {

    private DemoMatcherService demo = new DemoMatcherService();

    @TestableMock(targetMethod = "methodToBeMocked")
    private void methodWithoutArgument(DemoMatcherService self) {}

    @TestableMock(targetMethod = "methodToBeMocked")
    private void methodWithArguments(DemoMatcherService self, Object a1, Object a2) {}

    @TestableMock(targetMethod = "methodToBeMocked")
    private void methodWithArrayArgument(DemoMatcherService self, Object[] a) {}

    @Test
    void should_match_no_argument() {
        demo.callMethodWithoutArgument();
        verify("methodWithoutArgument").withTimes(1);
        demo.callMethodWithoutArgument();
        verify("methodWithoutArgument").withTimes(2);
    }

    @Test
    void should_match_number_arguments() {
        demo.callMethodWithNumberArguments();
        verify("methodWithArguments").without(anyString(), 2);
        verify("methodWithArguments").withInOrder(anyInt(), 2);
        verify("methodWithArguments").withInOrder(anyLong(), anyNumber());
        verify("methodWithArguments").with(1.0, anyMapOf(Integer.class, Float.class));
        verify("methodWithArguments").with(anyList(), anySetOf(Float.class));
        verify("methodWithArguments").with(anyList(), anyListOf(Float.class));
        verify("methodWithArrayArgument").with(anyArrayOf(Long.class));
        verify("methodWithArrayArgument").with(anyArray());
    }

    @Test
    void should_match_string_arguments() {
        demo.callMethodWithStringArgument();
        verify("methodWithArguments").with(startsWith("he"), endsWith("ld"));
        verify("methodWithArguments").with(contains("stab"), matches("m.[cd]k"));
        verify("methodWithArrayArgument").with(anyArrayOf(String.class));
    }

    @Test
    void should_match_object_arguments() {
        demo.callMethodWithObjectArgument();
        verify("methodWithArguments").withInOrder(any(BlackBox.class), any(BlackBox.class));
        verify("methodWithArguments").withInOrder(nullable(BlackBox.class), nullable(BlackBox.class));
        verify("methodWithArguments").withInOrder(isNull(), notNull());
    }

}
