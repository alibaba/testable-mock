package com.alibaba.demo.one2multi;

import com.alibaba.testable.core.annotation.MockMethod;

public class ASvcMock {

    @MockMethod(targetClass = String.class, targetMethod = "format")
    public String a_format(String format, Object... args) {
        return "a_mock";
    }

}
