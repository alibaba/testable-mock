package com.alibaba.demo.one2multi

import com.alibaba.testable.core.annotation.MockMethod

class BSvcMock {

    @MockMethod(targetClass = String::class, targetMethod = "format")
    fun b_format(format: String, vararg args: Any?): String {
        return "b_mock"
    }

}
