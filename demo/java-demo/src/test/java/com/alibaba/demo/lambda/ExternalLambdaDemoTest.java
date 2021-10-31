package com.alibaba.demo.lambda;

import com.alibaba.testable.core.annotation.MockMethod;
import org.junit.jupiter.api.Test;

import static com.alibaba.testable.core.matcher.InvokeVerifier.verify;

/**
 * @author zcbbpo
 */
public class ExternalLambdaDemoTest {
    private final ExternalLambdaDemo lambdaDemo = new ExternalLambdaDemo();

    @SuppressWarnings("unused")
    public static class Mock {

        @MockMethod(targetClass = String.class, targetMethod = "contains")
        public boolean mockContains(CharSequence s) {
            return false;
        }

        @MockMethod(targetClass = Byte.class, targetMethod = "floatValue")
        public float mockFloatValue() {
            return 0.1f;
        }

        @MockMethod(targetClass = Double.class, targetMethod = "compareTo")
        public int mockCompareTo(Double anotherDouble) {
            return 1;
        }

        @MockMethod(targetClass = LambdaDemo.class, targetMethod = "methodReference0")
        public String mockMethodReference0() {
            return "";
        }

        @MockMethod(targetClass = ExternalLambdaDemo.class, targetMethod = "f3")
        public Boolean mockF3(String s1, Long l) {
            return true;
        }
    }


    @Test
    public void shouldMockString1() {
        lambdaDemo.string1();
        verify("mockContains").withTimes(1);
    }

    @Test
    public void shouldMockByte1() {
        lambdaDemo.byte1();
        verify("mockFloatValue").withTimes(1);
    }

    @Test
    public void shouldMockDouble2() {
        lambdaDemo.double2();
        verify("mockCompareTo").withTimes(1);
    }

    @Test
    public void testMul() {
        lambdaDemo.mul();
        verify("mockContains").withTimes(2);
    }

    @Test
    public void testExternalClass() {
        lambdaDemo.externalClass();
        verify("mockMethodReference0").withTimes(1);
    }

    @Test
    public void testFunction3() {
        lambdaDemo.function3();
        verify("mockF3").withTimes(1);
    }

}
