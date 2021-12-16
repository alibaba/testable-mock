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

    private StaticInstanceReference instance = new StaticInstanceReference();

    //@MockDiagnose(LogLevel.VERBOSE)
    public static class Mock {
        @MockInvoke(targetClass = StaticInstanceReference.A.class, targetMethod = "doIt")
        private void mockDoIt() {
        }
    }

    @Test
    public void shouldMockT1() {
        instance.staticMethodReference();
        verifyInvoked("mockDoIt").withTimes(1);
    }
}
