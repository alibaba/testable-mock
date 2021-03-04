package com.alibaba.demo.one2multi

import com.alibaba.testable.core.annotation.MockMethod

class CSvcMock {

    @MockMethod(targetClass = String::class, targetMethod = "format")
    fun c_format(format: String, vararg args: Any?): String {
        return "c_mock"
    }

}
