package com.alibaba.testable.demo.service;

import com.alibaba.testable.core.accessor.PrivateAccessor;
import com.alibaba.testable.processor.annotation.EnablePrivateAccess;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnablePrivateAccess
class DemoPrivateAccessServiceTest {

    private DemoPrivateAccessService demoService = new DemoPrivateAccessService();

    @Test
    void should_able_to_access_private_method() throws Exception {
        assertEquals("hello - 1", demoService.privateFunc("hello", 1));
        assertEquals("hello - 1", PrivateAccessor.invoke(demoService, "privateFunc", "hello", 1));
    }

    @Test
    void should_able_to_access_private_field() throws Exception {
        demoService.count = 2;
        assertEquals("4", demoService.privateFieldAccessFunc());
        assertEquals(new Integer(4), demoService.count);

        PrivateAccessor.set(demoService, "count", 3);
        assertEquals("5", demoService.privateFieldAccessFunc());
        assertEquals(new Integer(5), PrivateAccessor.get(demoService, "count"));
    }

    @Test
    void should_able_to_update_final_field() throws Exception {
        demoService.pi = 4.13;
        assertEquals(4.13, demoService.pi);
    }

}
