package com.alibaba.demo.basic

import com.alibaba.demo.basic.model.mock.BlackBox
import com.alibaba.demo.basic.model.mock.Box
import com.alibaba.demo.basic.model.mock.Color

/**
 * 演示父类变量引用子类对象时的Mock场景
 * Demonstrate scenario of mocking method from sub-type object referred by parent-type variable
 */
class DemoInherit {

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
