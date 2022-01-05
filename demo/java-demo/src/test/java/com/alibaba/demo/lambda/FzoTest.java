package com.alibaba.demo.lambda;

import com.alibaba.testable.core.annotation.MockDiagnose;
import com.alibaba.testable.core.annotation.MockInvoke;
import com.alibaba.testable.core.model.LogLevel;
import org.junit.jupiter.api.Test;

/**
 * @author jim
 */
public class FzoTest {
    Fzo f = new Fzo();
    @MockDiagnose(LogLevel.VERBOSE)
    public static class Mock {
        @MockInvoke(targetClass = Double.class, targetMethod = "hashCode")
        private int cc() {
            return 1;
        }
    }

    @Test
    public void shouldMockInterfaceStatic() {
        f.objectStaticMethodReference();
    }
}
