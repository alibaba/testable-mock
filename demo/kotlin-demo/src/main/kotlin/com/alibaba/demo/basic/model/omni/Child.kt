package com.alibaba.demo.basic.model.omni

/**
 * 我是一个只有私有构造方法的类
 * This class have only private constructor
 */
class Child private constructor() {

    // ---------- 内部成员字段 ----------
    var grandChild: GrandChild? = null
    var subChild: InnerChild? = null

    /**
     * 这是一个私有内部类
     * An private inner class
     */
    inner class InnerChild {
        private val secret: String? = null
    }
}
