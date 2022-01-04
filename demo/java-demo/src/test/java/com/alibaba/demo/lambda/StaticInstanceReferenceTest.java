package com.alibaba.demo.lambda;

import com.alibaba.testable.core.annotation.MockDiagnose;
import com.alibaba.testable.core.annotation.MockInvoke;
import com.alibaba.testable.core.model.LogLevel;
import org.junit.jupiter.api.Test;

import static com.alibaba.testable.core.matcher.InvocationVerifier.verifyInvoked;


/**
 * @author jim
 */
public class StaticInstanceReferenceTest {

    private final StaticInstanceReference instance = new StaticInstanceReference();

    //@MockDiagnose(LogLevel.VERBOSE)
    public static class Mock {
        @MockInvoke(targetClass = StaticInstanceReference.StaticClassA.class, targetMethod = "doIt")
        private void mockDoIt() {
        }

        @MockInvoke(targetClass = StaticInstanceReference.ILambda.class, targetMethod = "run")
        private void mockFooRun() {
        }

        @MockInvoke(targetClass = StaticInstanceReference.ILambda.class, targetMethod = "function1")
        private void mockIFunction1(String s) {
        }
    }

    @Test
    public void shouldMockT1() {
        instance.staticMethodReference();
        verifyInvoked("mockDoIt").withTimes(2);
    }

    @Test
    public void shouldMockFooRun() {
        instance.interfaceDefault();
        verifyInvoked("mockFooRun").withTimes(1);
        verifyInvoked("mockIFunction1").withTimes(1);
    }
}
