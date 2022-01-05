package com.alibaba.demo.lambda;

import com.alibaba.testable.core.annotation.MockDiagnose;
import com.alibaba.testable.core.annotation.MockInvoke;
import com.alibaba.testable.core.model.LogLevel;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.stream.Stream;

import static com.alibaba.testable.core.matcher.InvocationVerifier.verifyInvoked;


/**
 * @author jim
 */
public class StaticInstanceReferenceTest {

    private final StaticInstanceReference instance = new StaticInstanceReference();

    @MockDiagnose(LogLevel.VERBOSE)
    public static class Mock {
        @MockInvoke(targetClass = StaticInstanceReference.StaticClassA.class, targetMethod = "doIt")
        private void mockDoIt() {
        }

        @MockInvoke(targetClass = StaticInstanceReference.ILambda.class, targetMethod = "run")
        private void mockILambdaRun() {
        }

        @MockInvoke(targetClass = StaticInstanceReference.ILambda.class, targetMethod = "function1")
        private void mockILambdaFunction1(String s) {
        }

        @MockInvoke(targetClass = Collection.class, targetMethod = "stream")
        <E> Stream<E> mockStream() {
            return null;
        }
    }

    @Test
    public void shouldMockDoIt() {
        instance.staticMethodReference();
        verifyInvoked("mockDoIt").withTimes(1);
    }

    @Test
    public void shouldMockCollectionStream() {
        instance.collectionInterfaceDefaultOrStatic();
        verifyInvoked("mockStream").withTimes(1);
    }

    @Test
    public void shouldMockInterfaceDefault() {
        instance.interfaceDefault();
        verifyInvoked("mockILambdaRun").withTimes(1);
        verifyInvoked("mockILambdaFunction1").withTimes(1);
    }

    @Test
    public void shouldMockObjectStaticMethodReference() {
        instance.objectStaticMethodReference();
    }

    @Test
    public void shouldMockInterfaceStatic() {
        instance.interfaceStatic();
    }
}
