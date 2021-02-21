package com.alibaba.testable.demo.one2multi

class ASvc {

    fun demo(name: String): String {
        return String.format("a_%s", name)
    }

}
