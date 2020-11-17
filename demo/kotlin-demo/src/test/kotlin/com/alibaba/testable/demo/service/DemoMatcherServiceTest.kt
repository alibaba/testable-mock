package com.alibaba.testable.demo.service

import com.alibaba.testable.core.annotation.TestableMock
import com.alibaba.testable.core.matcher.InvokeMatcher
import com.alibaba.testable.core.matcher.InvokeVerifier
import com.alibaba.testable.demo.model.BlackBox
import org.junit.jupiter.api.Test


internal class DemoMatcherServiceTest {

    @TestableMock(targetMethod = "methodToBeMocked")
    private fun methodWithoutArgument(self: DemoMatcherService) {
    }

    @TestableMock(targetMethod = "methodToBeMocked")
    private fun methodWithArguments(self: DemoMatcherService, a1: Any, a2: Any) {
    }

    @TestableMock(targetMethod = "methodToBeMocked")
    private fun methodWithArrayArgument(self: DemoMatcherService, a: Array<Any>) {
    }

    private val demo = DemoMatcherService()

    @Test
    fun should_match_no_argument() {
        demo.callMethodWithoutArgument()
        InvokeVerifier.verify("methodWithoutArgument").withTimes(1)
        demo.callMethodWithoutArgument()
        InvokeVerifier.verify("methodWithoutArgument").withTimes(2)
    }

    @Test
    fun should_match_number_arguments() {
        demo.callMethodWithNumberArguments()
        InvokeVerifier.verify("methodWithArguments").without(InvokeMatcher.anyString(), 2)
        InvokeVerifier.verify("methodWithArguments").withInOrder(InvokeMatcher.anyInt(), 2)
        InvokeVerifier.verify("methodWithArguments").withInOrder(InvokeMatcher.anyLong(), InvokeMatcher.anyNumber())
        // Note: Must use `::class.javaObjectType` for primary types check in Kotlin
        InvokeVerifier.verify("methodWithArguments").with(1.0, InvokeMatcher.anyMapOf(Int::class.javaObjectType, Float::class.javaObjectType))
        InvokeVerifier.verify("methodWithArguments").with(InvokeMatcher.anyList(), InvokeMatcher.anySetOf(Float::class.javaObjectType))
        InvokeVerifier.verify("methodWithArguments").with(InvokeMatcher.anyList(), InvokeMatcher.anyListOf(Float::class.javaObjectType))
        InvokeVerifier.verify("methodWithArrayArgument").with(InvokeMatcher.anyArrayOf(Long::class.javaObjectType))
        InvokeVerifier.verify("methodWithArrayArgument").with(InvokeMatcher.anyArray())
    }

    @Test
    fun should_match_string_arguments() {
        demo.callMethodWithStringArgument()
        InvokeVerifier.verify("methodWithArguments").with(InvokeMatcher.startsWith("he"), InvokeMatcher.endsWith("ld"))
        InvokeVerifier.verify("methodWithArguments").with(InvokeMatcher.contains("stab"), InvokeMatcher.matches("m.[cd]k"))
        InvokeVerifier.verify("methodWithArrayArgument").with(InvokeMatcher.anyArrayOf(String::class.java))
    }

    @Test
    fun should_match_object_arguments() {
        demo.callMethodWithObjectArgument()
        InvokeVerifier.verify("methodWithArguments").withInOrder(InvokeMatcher.any(BlackBox::class.java), InvokeMatcher.any(BlackBox::class.java))
        InvokeVerifier.verify("methodWithArguments").withInOrder(InvokeMatcher.nullable(BlackBox::class.java), InvokeMatcher.nullable(BlackBox::class.java))
        InvokeVerifier.verify("methodWithArguments").withInOrder(InvokeMatcher.isNull(), InvokeMatcher.notNull())
    }
}
