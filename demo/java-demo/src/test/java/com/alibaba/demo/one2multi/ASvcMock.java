package com.alibaba.demo.one2multi;

import com.alibaba.testable.core.annotation.MockInvoke;

public class ASvcMock {

    @MockInvoke(targetClass = String.class, targetMethod = "format")
    public String a_format(String format, Object... args) {
        return "a_mock";
    }

}
