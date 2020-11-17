package com.alibaba.testable.demo.service

import com.alibaba.testable.demo.model.BlackBox
import org.springframework.stereotype.Service
import java.util.*

/**
 * @author flin
 */
@Service
class DemoMatcherService {
    /**
     * Method to be mocked
     */
    private fun methodToBeMocked() {
        // pretend to have some code here
    }

    /**
     * Method to be mocked
     */
    private fun methodToBeMocked(a1: Any?, a2: Any?) {
        // pretend to have some code here
    }

    /**
     * Method to be mocked
     */
    private fun methodToBeMocked(a: Array<out Any>) {
        // pretend to have some code here
    }

    fun callMethodWithoutArgument() {
        methodToBeMocked()
    }

    fun callMethodWithNumberArguments() {
        // named variable and lambda variable will be recorded as different type
        // should have them both in test case
        val floatList: MutableList<Float> = ArrayList()
        floatList.add(1.0f)
        floatList.add(2.0f)
        val longArray = arrayOf(1L, 2L)
        methodToBeMocked(1, 2)
        methodToBeMocked(1L, 2.0)
        methodToBeMocked(listOf(1), setOf(1.0f))
        methodToBeMocked(1.0, mapOf(1 to 1.0f))
        methodToBeMocked(floatList, floatList)
        methodToBeMocked(longArray)
        methodToBeMocked(arrayOf(1.0, 2.0))
    }

    fun callMethodWithStringArgument() {
        methodToBeMocked("hello", "world")
        methodToBeMocked("testable", "mock")
        methodToBeMocked(arrayOf("demo"))
    }

    fun callMethodWithObjectArgument() {
        methodToBeMocked(BlackBox("hello"), BlackBox("world"))
        methodToBeMocked(BlackBox("demo"), null)
        methodToBeMocked(null, BlackBox("demo"))
    }
}
