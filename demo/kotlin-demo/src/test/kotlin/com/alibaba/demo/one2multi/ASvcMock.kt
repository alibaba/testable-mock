package com.alibaba.demo.one2multi

import com.alibaba.testable.core.annotation.MockInvoke

class ASvcMock {

    @MockInvoke(targetClass = String::class, targetMethod = "format")
    fun a_format(format: String, vararg args: Any?): String {
        return "a_mock"
    }

}
