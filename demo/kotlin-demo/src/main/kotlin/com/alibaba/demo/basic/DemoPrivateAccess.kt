package com.alibaba.demo.basic

/**
 * 演示私有成员访问功能
 * Demonstrate private member access functionality
 */
class DemoPrivateAccess {

    /**
     * a private member field
     */
    var count = 0

    /**
     * a constant field
     */
    val pi = 3.14

    /**
     * private member method
     */
    fun privateFunc(list: List<String>, str: String, i: Int): String {
        return list.reduce { a: String, s: String -> a + s } + " + " + "$str + $i"
    }

    companion object {

        /**
         * a private static field
         */
        var staticCount = 0

        /**
         * private static method
         */
        fun privateStaticFunc(str: String, i: Int): String {
            return "$str + $i"
        }

        /**
         * private jvm static method
         */
        @JvmStatic private fun privateJvmStaticFunc(list: List<String>, str: String, i: Int): String {
            return list.reduce { a: String, s: String -> a + s } + " * " + "$str * $i"
        }
    }

}
