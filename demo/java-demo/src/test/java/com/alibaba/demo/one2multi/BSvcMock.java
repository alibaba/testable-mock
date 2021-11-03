package com.alibaba.demo.one2multi;

import com.alibaba.testable.core.annotation.MockInvoke;

public class BSvcMock {

    @MockInvoke(targetClass = String.class, targetMethod = "format")
    public String b_format(String format, Object... args) {
        return "b_mock";
    }

}
