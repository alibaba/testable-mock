package com.alibaba.demo.basic.issue;

import com.alibaba.testable.core.annotation.MockInvoke;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceATest {

    private ServiceA sa = new ServiceA();

    public static class Mock {
        @MockInvoke(targetClass = AbstractServiceA.class, targetMethod = "put")
        public String put(Object input) {
            return "mocked";
        }
    }

    @Test
    void get() {
        assertEquals("mocked", sa.get(123));
    }
}