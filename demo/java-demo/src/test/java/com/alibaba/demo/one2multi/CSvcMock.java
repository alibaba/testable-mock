package com.alibaba.demo.one2multi;

import com.alibaba.testable.core.annotation.MockInvoke;

public class CSvcMock {

    @MockInvoke(targetClass = String.class, targetMethod = "format")
    public String c_format(String format, Object... args) {
        return "c_mock";
    }

}
