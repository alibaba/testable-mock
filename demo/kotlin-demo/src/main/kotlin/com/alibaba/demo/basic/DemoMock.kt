package com.alibaba.demo.basic

import com.alibaba.demo.basic.model.mock.BlackBox
import com.alibaba.demo.basic.model.mock.ColorBox
import java.nio.file.Files
import java.nio.file.Paths

/**
 * 演示基本的Mock功能
 * Demonstrate basic mock functionality
 */
class DemoMock {

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
        return "{ \"res\": \"" + innerFunc(s) + staticFunc() + "\"}"
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

    private fun innerFunc(s: String): String {
        return Files.readAllLines(Paths.get("/a-not-exist-file")).joinToString()
    }

    private fun callFromDifferentMethod() = "realOne"

    companion object {
        private fun staticFunc(): String {
            return "_STATIC_TAIL"
        }

//        fun callStaticFunc(): String {
//            return "CALL${staticFunc()}"
//        }
    }
}
