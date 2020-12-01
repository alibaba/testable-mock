package com.alibaba.testable.demo

class DemoPrivateAccess {

    /**
     * a private member field
     */
    private var count = 0

    /**
     * a constant field
     */
    val pi = 3.14

    /**
     * private member method
     */
    private fun privateFunc(s: String, i: Int): String {
        return "$s - $i"
    }

    companion object {

        /**
         * a private static field
         */
        private var staticCount = 0

        /**
         * private static method
         */
        private fun privateStaticFunc(s: String, i: Int): String {
            return "$s + $i"
        }

        /**
         * private jvm static method
         */
        @JvmStatic private fun privateJvmStaticFunc(s: String, i: Int): String {
            return "$s * $i"
        }
    }

}
