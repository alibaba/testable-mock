package com.alibaba.testable.demo.one2multi

class CSvc {

    fun demo(name: String): String {
        return String.format("c_%s", name)
    }

}
