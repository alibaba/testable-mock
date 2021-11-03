package com.alibaba.demo.lambda;

import com.alibaba.testable.core.annotation.MockInvoke;
import org.junit.jupiter.api.Test;

import static com.alibaba.testable.core.matcher.InvocationVerifier.verifyInvoked;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author zcbbpo
 */
public class LambdaDemoTest {
    private final LambdaDemo lambdaDemo = new LambdaDemo();

    @SuppressWarnings("unused")
    public static class Mock {
        @MockInvoke(targetClass = LambdaDemo.class, targetMethod = "run")
        private void mockRun() {
        }

        @MockInvoke(targetClass = LambdaDemo.class)
        private String function0() {
            return "mock_function0";
        }

        @MockInvoke(targetClass = LambdaDemo.class)
        private String function1(Integer i) {
            return "mock_function1";
        }

        @MockInvoke(targetClass = LambdaDemo.class)
        private String function2(Integer i, Double d) {
            return "mock_function2";
        }

        @SuppressWarnings("RedundantThrows")
        @MockInvoke(targetClass = LambdaDemo.class)
        private String function1Throwable(Integer i) throws Throwable{
            return "mock_function1Throwable";
        }

        @MockInvoke(targetClass = StaticMethod.class, targetMethod = "function1")
        public static String staticFunction1(Integer i) {
            return "mock_staticFunction1";
        }


    }

    @Test
    public void shouldMockRun() {
        lambdaDemo.methodReference();
        verifyInvoked("mockRun").withTimes(1);
    }

    @Test
    public void shouldMockFunction0() {
        String s = lambdaDemo.methodReference0();
        assertEquals(s, "mock_function0");
    }

    @Test
    public void shouldMockFunction1() {
        String s = lambdaDemo.methodReference1();
        assertEquals(s, "mock_function1");
    }

    @Test
    public void shouldMockFunction2() {
        String s = lambdaDemo.methodReference2();
        assertEquals(s, "mock_function2");
    }

    @Test
    public void shouldMockFunction1Throws() {
        String s = lambdaDemo.methodReferenceThrows();
        assertEquals(s, "mock_function1Throwable");
    }

    @Test
    public void shouldMockStaticFunction1() {
        String s = lambdaDemo.staticMethodReference1();
        assertEquals(s, "mock_staticFunction1");
    }

}
