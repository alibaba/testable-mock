package com.alibaba.testable.demo.service

import org.springframework.stereotype.Service


@Service
class DemoPrivateAccessService {

    private var count = 0

    final val pi = 3.14

    /**
     * private method
     */
    private fun privateFunc(s: String, i: Int): String {
        return "$s - $i"
    }

    /**
     * method with private field access
     */
    fun privateFieldAccessFunc(): String {
        count += 2
        return count.toString()
    }

}
