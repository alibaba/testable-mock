package com.alibaba.demo.one2multi;

import com.alibaba.testable.core.annotation.MockWith;
import org.junit.jupiter.api.Test;


import static com.alibaba.testable.core.matcher.InvocationVerifier.verifyInvoked;
import static org.junit.jupiter.api.Assertions.assertEquals;

@MockWith
public class OneToMultiSvcTest {

    private ASvc aSvc = new ASvc();
    private BSvc bSvc = new BSvc();
    private CSvc cSvc = new CSvc();

    @Test
    public void should_test_multi_class_together() {
        assertEquals("a_mock", aSvc.demo("test"));
        assertEquals("b_mock", bSvc.demo("test"));
        assertEquals("c_mock", cSvc.demo("test"));
        verifyInvoked("a_format").withTimes(1);
        verifyInvoked("b_format").withTimes(1);
        verifyInvoked("c_format").withTimes(1);
    }

}
