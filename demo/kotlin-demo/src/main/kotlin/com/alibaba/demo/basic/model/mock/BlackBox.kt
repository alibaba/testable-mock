package com.alibaba.demo.basic.model.mock


class BlackBox(var input: String) : Box(), Color {

    init {
        this.content = input
    }

    override fun put(something: String) {
        content = something
    }

    override val color: String
        get() = "black"

    fun trim(): String? {
        return content?.trim()
    }

    fun substring(from: Int, to: Int): String? {
        return content?.substring(from, to)
    }

    fun startsWith(prefix: String): Boolean {
        return content?.startsWith(prefix) == true
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
