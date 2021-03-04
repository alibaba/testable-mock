package com.alibaba.demo.one2multi

import com.alibaba.testable.core.annotation.MockWith
import com.alibaba.testable.core.matcher.InvokeVerifier.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@MockWith
class OneToMultiSvcTest {

    private val aSvc = ASvc()
    private val bSvc = BSvc()
    private val cSvc = CSvc()

    @Test
    fun should_able_to_test_multi_class_together() {
        Assertions.assertEquals("a_mock", aSvc.demo("test"))
        Assertions.assertEquals("b_mock", bSvc.demo("test"))
        Assertions.assertEquals("c_mock", cSvc.demo("test"))
        verify("a_format").withTimes(1)
        verify("b_format").withTimes(1)
        verify("c_format").withTimes(1)
    }

}
