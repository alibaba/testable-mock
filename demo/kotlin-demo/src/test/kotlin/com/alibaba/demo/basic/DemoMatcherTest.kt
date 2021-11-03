package com.alibaba.demo.basic

import com.alibaba.testable.core.annotation.MockInvoke
import com.alibaba.testable.core.error.VerifyFailedError
import com.alibaba.testable.core.matcher.InvocationMatcher
import com.alibaba.testable.core.matcher.InvocationVerifier
import com.alibaba.demo.basic.model.mock.BlackBox
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * 演示Mock方法调用校验器
 * Demonstrate mock method invocation verifier
 */
internal class DemoMatcherTest {

    private val demoMatcher = DemoMatcher()

    class Mock {
        @MockInvoke(targetMethod = "methodToBeMocked")
        private fun methodWithoutArgument(self: DemoMatcher) {
        }

        @MockInvoke(targetMethod = "methodToBeMocked")
        private fun methodWithArguments(self: DemoMatcher, a1: Any, a2: Any) {
        }

        @MockInvoke(targetMethod = "methodToBeMocked")
        private fun methodWithArrayArgument(self: DemoMatcher, a: Array<Any>) {
        }
    }

    @Test
    fun should_match_no_argument() {
        demoMatcher.callMethodWithoutArgument()
        InvocationVerifier.verifyInvoked("methodWithoutArgument").withTimes(1)
        demoMatcher.callMethodWithoutArgument()
        InvocationVerifier.verifyInvoked("methodWithoutArgument").withTimes(2)
    }

    @Test
    fun should_match_number_arguments() {
        demoMatcher.callMethodWithNumberArguments()
        InvocationVerifier.verifyInvoked("methodWithArguments").without(InvocationMatcher.anyString(), 2)
        InvocationVerifier.verifyInvoked("methodWithArguments").withInOrder(InvocationMatcher.anyInt(), 2)
        InvocationVerifier.verifyInvoked("methodWithArguments").withInOrder(InvocationMatcher.anyLong(), InvocationMatcher.anyNumber())
        // Note: Must use `::class.javaObjectType` for primary types check in Kotlin
        InvocationVerifier.verifyInvoked("methodWithArguments").with(1.0, InvocationMatcher.anyMapOf(Int::class.javaObjectType, Float::class.javaObjectType)).times(2)
        InvocationVerifier.verifyInvoked("methodWithArguments").with(InvocationMatcher.anyList(), InvocationMatcher.anySetOf(Float::class.javaObjectType)).times(2)
        InvocationVerifier.verifyInvoked("methodWithArguments").with(InvocationMatcher.anyList(), InvocationMatcher.anyListOf(Float::class.javaObjectType))
        InvocationVerifier.verifyInvoked("methodWithArrayArgument").with(InvocationMatcher.anyArrayOf(Long::class.javaObjectType))
        InvocationVerifier.verifyInvoked("methodWithArrayArgument").with(InvocationMatcher.anyArray())
    }

    @Test
    fun should_match_string_arguments() {
        demoMatcher.callMethodWithStringArgument()
        InvocationVerifier.verifyInvoked("methodWithArguments").with(InvocationMatcher.startsWith("he"), InvocationMatcher.endsWith("ld"))
        InvocationVerifier.verifyInvoked("methodWithArguments").with(InvocationMatcher.contains("stab"), InvocationMatcher.matches("m.[cd]k"))
        InvocationVerifier.verifyInvoked("methodWithArrayArgument").with(InvocationMatcher.anyArrayOf(String::class.java))
    }

    @Test
    fun should_match_object_arguments() {
        demoMatcher.callMethodWithObjectArgument()
        InvocationVerifier.verifyInvoked("methodWithArguments").withInOrder(InvocationMatcher.any(BlackBox::class.java), InvocationMatcher.any(BlackBox::class.java))
        InvocationVerifier.verifyInvoked("methodWithArguments").withInOrder(InvocationMatcher.nullable(BlackBox::class.java), InvocationMatcher.nullable(BlackBox::class.java))
        InvocationVerifier.verifyInvoked("methodWithArguments").withInOrder(InvocationMatcher.isNull(), InvocationMatcher.notNull())
    }

    @Test
    fun should_match_with_times() {
        demoMatcher.callMethodWithNumberArguments()
        InvocationVerifier.verifyInvoked("methodWithArguments").with(InvocationMatcher.anyNumber(), InvocationMatcher.any()).times(4)

        demoMatcher.callMethodWithNumberArguments()
        var gotError = false
        try {
            InvocationVerifier.verifyInvoked("methodWithArguments").with(InvocationMatcher.anyNumber(), InvocationMatcher.any()).times(5)
        } catch (e: VerifyFailedError) {
            gotError = true
        }
        if (!gotError) {
            Assertions.fail<Any>()
        }
    }
}
