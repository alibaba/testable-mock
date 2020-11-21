package com.alibaba.testable.demo.service

import com.alibaba.testable.demo.model.BlackBox
import com.alibaba.testable.demo.model.Box
import com.alibaba.testable.demo.model.ColorBox
import org.springframework.stereotype.Service
import sun.net.www.http.HttpClient
import java.net.URL


@Service
class DemoMockService {

    /**
     * method with new operation
     */
    fun newFunc(): String? {
        return BlackBox("something").get()
    }

    /**
     * method with member method invoke
     */
    fun outerFunc(s: String): String {
        return "{ \"res\": \"" + innerFunc(s) + "\"}"
    }

    /**
     * method with common method invoke
     */
    fun commonFunc(): String {
        val box = BlackBox("anything")
        return box.trim() + "__" + box.substring(1, 2) + "__" + box.startsWith("any")
    }

    /**
     * method with static method invoke
     */
    fun getBox(): BlackBox {
        return ColorBox.createBox("Red", BlackBox.secretBox())
    }

    /**
     * two methods invoke same private method
     */
    fun callerOne(): String {
        return callFromDifferentMethod()
    }

    fun callerTwo(): String {
        return callFromDifferentMethod()
    }

    private fun innerFunc(s: String) = HttpClient.New(URL("http:/xxx/$s")).urlFile

    private fun callFromDifferentMethod() = "realOne"
}
