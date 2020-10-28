package com.alibaba.testable.demo.model


class BlackBox(private var data: String) : Box {

    override fun put(something: String) {
        data = something
    }

    fun get(): String {
        return data
    }

    fun trim(): String {
        return data.trim()
    }

    fun substring(from: Int, to: Int): String {
        return data.substring(from, to)
    }

    fun startsWith(prefix: String): Boolean {
        return data.startsWith(prefix)
    }

    companion object {
        fun secretBox(): BlackBox {
            return BlackBox("secret")
        }
    }

}

object ColorBox {
    fun createBox(color: String, box: BlackBox): BlackBox {
        return BlackBox("${color}_${box.get()}")
    }
}
