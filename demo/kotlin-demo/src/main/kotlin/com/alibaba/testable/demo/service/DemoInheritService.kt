package com.alibaba.testable.demo.service

import com.alibaba.testable.demo.model.BlackBox
import com.alibaba.testable.demo.model.Box
import com.alibaba.testable.demo.model.Color
import org.springframework.stereotype.Service

@Service
class DemoInheritService {

    /**
     * call method overridden by sub class via parent class variable
     */
    fun putIntoBox(): Box {
        val box: Box = BlackBox("")
        box.put("data")
        return box
    }

    /**
     * call method overridden by sub class via sub class variable
     */
    fun putIntoBlackBox(): BlackBox {
        val box = BlackBox("")
        box.put("data")
        return box
    }

    /**
     * call method defined in parent class via parent class variable
     */
    val fromBox: String?
        get() {
            val box: Box = BlackBox("data")
            return box.get()
        }

    /**
     * call method defined in parent class via sub class variable
     */
    val fromBlackBox: String?
        get() {
            val box = BlackBox("data")
            return box.get()
        }

    /**
     * call method defined in interface via interface variable
     */
    val colorViaColor: String
        get() {
            val color: Color = BlackBox("")
            return color.color
        }

    /**
     * call method defined in interface via sub class variable
     */
    val colorViaBox: String
        get() {
            val box = BlackBox("")
            return box.color
        }
}
