package com.alibaba.demo.lambda;

import com.alibaba.testable.core.annotation.MockInvoke;
import org.junit.jupiter.api.Test;

import static com.alibaba.testable.core.matcher.InvocationVerifier.verifyInvoked;

/**
 * @author zcbbpo
 */
public class ExternalLambdaDemoTest {
    private final ExternalLambdaDemo lambdaDemo = new ExternalLambdaDemo();

    @SuppressWarnings("unused")
    public static class Mock {

        @MockInvoke(targetClass = String.class, targetMethod = "contains")
        public boolean mockContains(CharSequence s) {
            return false;
        }

        @MockInvoke(targetClass = Byte.class, targetMethod = "floatValue")
        public float mockFloatValue() {
            return 0.1f;
        }

        @MockInvoke(targetClass = Double.class, targetMethod = "compareTo")
        public int mockCompareTo(Double anotherDouble) {
            return 1;
        }

        @MockInvoke(targetClass = LambdaDemo.class, targetMethod = "methodReference0")
        public String mockMethodReference0() {
            return "";
        }

        @MockInvoke(targetClass = ExternalLambdaDemo.class, targetMethod = "f3")
        public Boolean mockF3(String s1, Long l) {
            return true;
        }
    }


    @Test
    public void shouldMockString1() {
        lambdaDemo.string1();
        verifyInvoked("mockContains").withTimes(1);
    }

    @Test
    public void shouldMockByte1() {
        lambdaDemo.byte1();
        verifyInvoked("mockFloatValue").withTimes(1);
    }

    @Test
    public void shouldMockDouble2() {
        lambdaDemo.double2();
        verifyInvoked("mockCompareTo").withTimes(1);
    }

    @Test
    public void testMul() {
        lambdaDemo.mul();
        verifyInvoked("mockContains").withTimes(2);
    }

    @Test
    public void testExternalClass() {
        lambdaDemo.externalClass();
        verifyInvoked("mockMethodReference0").withTimes(1);
    }

    @Test
    public void testFunction3() {
        lambdaDemo.function3();
        verifyInvoked("mockF3").withTimes(1);
    }

}
