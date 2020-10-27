package com.alibaba.testable.demo

import org.springframework.stereotype.Service
import sun.net.www.http.HttpClient
import java.net.URL


@Service
class DemoService {

    private var count = 0

    /**
     * Target 1 - private method
     */
    private fun privateFunc(s: String, i: Int): String {
        return "$s - $i"
    }

    /**
     * Target 2 - method with private field access
     */
    fun privateFieldAccessFunc(): String {
        count += 2
        return count.toString()
    }

    /**
     * Target 3 - method with new operation
     */
    fun newFunc(): String {
        return BlackBox("something").callMe()
    }

    /**
     * Target 4 - method with member method invoke
     */
    fun outerFunc(s: String): String {
        return "{ \"res\": \"" + innerFunc(s) + "\"}"
    }

    /**
     * Target 5 - method with common method invoke
     */
    fun commonFunc(): String {
        val box = BlackBox("anything")
        return box.trim() + "__" + box.substring(1, 2) + "__" + box.startsWith("any")
    }

    /**
     * Target 6 - method with static method invoke
     */
    fun getBox(): BlackBox {
        return ColorBox.createBox("Red", BlackBox.secretBox())
    }

    fun callerOne(): String {
        return callFromDifferentMethod()
    }

    fun callerTwo(): String {
        return callFromDifferentMethod()
    }

    private fun innerFunc(s: String) = HttpClient.New(URL("http:/xxx/$s")).urlFile

    private fun callFromDifferentMethod() = "realOne"
}
