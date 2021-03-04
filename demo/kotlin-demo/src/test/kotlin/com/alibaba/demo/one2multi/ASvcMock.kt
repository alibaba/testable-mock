package com.alibaba.demo.one2multi

import com.alibaba.testable.core.annotation.MockMethod

class ASvcMock {

    @MockMethod(targetClass = String::class, targetMethod = "format")
    fun a_format(format: String, vararg args: Any?): String {
        return "a_mock"
    }

}
