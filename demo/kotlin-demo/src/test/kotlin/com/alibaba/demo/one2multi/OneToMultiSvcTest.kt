package com.alibaba.demo.one2multi

import com.alibaba.testable.core.annotation.MockWith
import com.alibaba.testable.core.matcher.InvocationVerifier.verifyInvoked
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@MockWith
class OneToMultiSvcTest {

    private val aSvc = ASvc()
    private val bSvc = BSvc()
    private val cSvc = CSvc()

    @Test
    fun should_test_multi_class_together() {
        assertEquals("a_mock", aSvc.demo("test"))
        assertEquals("b_mock", bSvc.demo("test"))
        assertEquals("c_mock", cSvc.demo("test"))
        verifyInvoked("a_format").withTimes(1)
        verifyInvoked("b_format").withTimes(1)
        verifyInvoked("c_format").withTimes(1)
    }

}
