package com.alibaba.demo.lambda;

import com.alibaba.testable.core.annotation.MockInvoke;
import org.junit.jupiter.api.Test;

import static com.alibaba.testable.core.matcher.InvocationVerifier.verifyInvoked;


/**
 * @author jim
 */
public class StaticInstanceReferenceTest {

    private final StaticInstanceReference instance = new StaticInstanceReference();

    public static class Mock {
        @MockInvoke(targetClass = StaticInstanceReference.StaticClassA.class, targetMethod = "doIt")
        private void mockDoIt() {
        }

        @MockInvoke(targetClass = StaticInstanceReference.StaticClassA.class)
        private Integer function2(String s) {
          return 2;
        }
    }

    @Test
    public void shouldMockDoIt() {
        instance.staticMethodReference();
        verifyInvoked("mockDoIt").withTimes(1);
        verifyInvoked("function2").withTimes(1);
    }
}
