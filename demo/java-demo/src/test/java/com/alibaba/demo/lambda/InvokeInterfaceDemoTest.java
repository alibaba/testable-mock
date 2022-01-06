package com.alibaba.demo.lambda;

import com.alibaba.testable.core.annotation.MockDiagnose;
import com.alibaba.testable.core.annotation.MockInvoke;
import com.alibaba.testable.core.model.LogLevel;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static com.alibaba.testable.core.matcher.InvocationVerifier.verifyInvoked;


/**
 * @author jim
 */
public class InvokeInterfaceDemoTest {

    private final InvokeInterfaceDemo instance = new InvokeInterfaceDemo();

    @MockDiagnose(LogLevel.VERBOSE)
    public static class Mock {

        @MockInvoke(targetClass = InvokeInterfaceDemo.ILambda.class, targetMethod = "run")
        private void mockILambdaRun() {
        }

        @MockInvoke(targetClass = InvokeInterfaceDemo.ILambda.class, targetMethod = "function1")
        private void mockILambdaFunction1(String s) {
        }

        @SuppressWarnings("unchecked")
        @MockInvoke(targetClass = Collection.class, targetMethod = "stream")
        <E> Stream<E> mockStream() {
            List<E> fooList = new ArrayList<>();
            fooList.add((E) "123");
            fooList.add((E) "456");
            return fooList.stream();
        }

        @MockInvoke(targetClass = Boolean.class, targetMethod = "logicalAnd")
        public static boolean mockLogicalAnd(boolean a, boolean b) {
            return false;
        }
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
        verifyInvoked("mockLogicalAnd").withTimes(2);
    }

    @Test
    public void shouldMockInterfaceStatic() {
        instance.interfaceStatic();
    }
}
