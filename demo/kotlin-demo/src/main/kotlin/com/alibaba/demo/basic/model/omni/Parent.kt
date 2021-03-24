package com.alibaba.demo.basic.model.omni

import java.lang.IllegalArgumentException

/**
 * 我是一个虽有构造方法，但无法正常构造的类
 * This class have constructor with exception throw
 */
class Parent {

    init {
        throw IllegalArgumentException()
    }

    // ---------- 内部成员字段 ----------
    var child: Child? = null
    var children: Array<Child>? = null

}
