package com.alibaba.demo.basic

import com.alibaba.testable.core.annotation.MockMethod
import com.alibaba.testable.core.error.VerifyFailedError
import com.alibaba.testable.core.matcher.InvokeMatcher
import com.alibaba.testable.core.matcher.InvokeVerifier
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
        @MockMethod(targetMethod = "methodToBeMocked")
        private fun methodWithoutArgument(self: DemoMatcher) {
        }

        @MockMethod(targetMethod = "methodToBeMocked")
        private fun methodWithArguments(self: DemoMatcher, a1: Any, a2: Any) {
        }

        @MockMethod(targetMethod = "methodToBeMocked")
        private fun methodWithArrayArgument(self: DemoMatcher, a: Array<Any>) {
        }
    }

    @Test
    fun should_match_no_argument() {
        demoMatcher.callMethodWithoutArgument()
        InvokeVerifier.verify("methodWithoutArgument").withTimes(1)
        demoMatcher.callMethodWithoutArgument()
        InvokeVerifier.verify("methodWithoutArgument").withTimes(2)
    }

    @Test
    fun should_match_number_arguments() {
        demoMatcher.callMethodWithNumberArguments()
        InvokeVerifier.verify("methodWithArguments").without(InvokeMatcher.anyString(), 2)
        InvokeVerifier.verify("methodWithArguments").withInOrder(InvokeMatcher.anyInt(), 2)
        InvokeVerifier.verify("methodWithArguments").withInOrder(InvokeMatcher.anyLong(), InvokeMatcher.anyNumber())
        // Note: Must use `::class.javaObjectType` for primary types check in Kotlin
        InvokeVerifier.verify("methodWithArguments").with(1.0, InvokeMatcher.anyMapOf(Int::class.javaObjectType, Float::class.javaObjectType)).times(2)
        InvokeVerifier.verify("methodWithArguments").with(InvokeMatcher.anyList(), InvokeMatcher.anySetOf(Float::class.javaObjectType)).times(2)
        InvokeVerifier.verify("methodWithArguments").with(InvokeMatcher.anyList(), InvokeMatcher.anyListOf(Float::class.javaObjectType))
        InvokeVerifier.verify("methodWithArrayArgument").with(InvokeMatcher.anyArrayOf(Long::class.javaObjectType))
        InvokeVerifier.verify("methodWithArrayArgument").with(InvokeMatcher.anyArray())
    }

    @Test
    fun should_match_string_arguments() {
        demoMatcher.callMethodWithStringArgument()
        InvokeVerifier.verify("methodWithArguments").with(InvokeMatcher.startsWith("he"), InvokeMatcher.endsWith("ld"))
        InvokeVerifier.verify("methodWithArguments").with(InvokeMatcher.contains("stab"), InvokeMatcher.matches("m.[cd]k"))
        InvokeVerifier.verify("methodWithArrayArgument").with(InvokeMatcher.anyArrayOf(String::class.java))
    }

    @Test
    fun should_match_object_arguments() {
        demoMatcher.callMethodWithObjectArgument()
        InvokeVerifier.verify("methodWithArguments").withInOrder(InvokeMatcher.any(BlackBox::class.java), InvokeMatcher.any(BlackBox::class.java))
        InvokeVerifier.verify("methodWithArguments").withInOrder(InvokeMatcher.nullable(BlackBox::class.java), InvokeMatcher.nullable(BlackBox::class.java))
        InvokeVerifier.verify("methodWithArguments").withInOrder(InvokeMatcher.isNull(), InvokeMatcher.notNull())
    }

    @Test
    fun should_match_with_times() {
        demoMatcher.callMethodWithNumberArguments()
        InvokeVerifier.verify("methodWithArguments").with(InvokeMatcher.anyNumber(), InvokeMatcher.any()).times(4)

        demoMatcher.callMethodWithNumberArguments()
        var gotError = false
        try {
            InvokeVerifier.verify("methodWithArguments").with(InvokeMatcher.anyNumber(), InvokeMatcher.any()).times(5)
        } catch (e: VerifyFailedError) {
            gotError = true
        }
        if (!gotError) {
            Assertions.fail<Any>()
        }
    }
}
