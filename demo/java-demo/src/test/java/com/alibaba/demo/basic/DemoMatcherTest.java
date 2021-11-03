package com.alibaba.demo.basic;

import com.alibaba.demo.basic.model.mock.BlackBox;
import com.alibaba.testable.core.annotation.MockInvoke;
import com.alibaba.testable.core.error.VerifyFailedError;
import org.junit.jupiter.api.Test;

import static com.alibaba.testable.core.matcher.InvocationMatcher.*;
import static com.alibaba.testable.core.matcher.InvocationVerifier.verifyInvoked;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * 演示Mock方法调用校验器
 * Demonstrate mock method invocation verifier
 */
class DemoMatcherTest {

    private DemoMatcher demoMatcher = new DemoMatcher();

    public static class Mock {
        @MockInvoke(targetMethod = "methodToBeMocked")
        private void methodWithoutArgument(DemoMatcher self) {}

        @MockInvoke(targetMethod = "methodToBeMocked")
        private void methodWithArguments(DemoMatcher self, Object a1, Object a2) {}

        @MockInvoke(targetMethod = "methodToBeMocked")
        private void methodWithArrayArgument(DemoMatcher self, Object[] a) {}
    }

    @Test
    void should_match_no_argument() {
        demoMatcher.callMethodWithoutArgument();
        verifyInvoked("methodWithoutArgument").withTimes(1);
        demoMatcher.callMethodWithoutArgument();
        verifyInvoked("methodWithoutArgument").withTimes(2);
    }

    @Test
    void should_match_number_arguments() {
        demoMatcher.callMethodWithNumberArguments();
        verifyInvoked("methodWithArguments").without(anyString(), 2);
        verifyInvoked("methodWithArguments").withInOrder(anyInt(), 2);
        verifyInvoked("methodWithArguments").withInOrder(anyLong(), anyNumber());
        verifyInvoked("methodWithArguments").with(1.0, anyMapOf(Integer.class, Float.class));
        verifyInvoked("methodWithArguments").with(anyList(), anySetOf(Float.class));
        verifyInvoked("methodWithArguments").with(anyList(), anyListOf(Float.class));
        verifyInvoked("methodWithArrayArgument").with(anyArrayOf(Long.class));
        verifyInvoked("methodWithArrayArgument").with(anyArray());
    }

    @Test
    void should_match_string_arguments() {
        demoMatcher.callMethodWithStringArgument();
        verifyInvoked("methodWithArguments").with(startsWith("he"), endsWith("ld"));
        verifyInvoked("methodWithArguments").with(contains("stab"), matches("m.[cd]k"));
        verifyInvoked("methodWithArrayArgument").with(anyArrayOf(String.class));
    }

    @Test
    void should_match_object_arguments() {
        demoMatcher.callMethodWithObjectArgument();
        verifyInvoked("methodWithArguments").withInOrder(any(BlackBox.class), any(BlackBox.class));
        verifyInvoked("methodWithArguments").withInOrder(nullable(BlackBox.class), nullable(BlackBox.class));
        verifyInvoked("methodWithArguments").withInOrder(isNull(), notNull());
    }

    @Test
    void should_match_with_times() {
        demoMatcher.callMethodWithNumberArguments();
        verifyInvoked("methodWithArguments").with(anyNumber(), any()).times(3);

        demoMatcher.callMethodWithNumberArguments();
        boolean gotError = false;
        try {
            verifyInvoked("methodWithArguments").with(anyNumber(), any()).times(4);
        } catch (VerifyFailedError e) {
            gotError = true;
        }
        if (!gotError) {
            fail();
        }
    }

}
