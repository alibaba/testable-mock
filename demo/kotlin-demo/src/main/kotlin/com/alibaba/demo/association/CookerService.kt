package com.alibaba.demo.association

/**
 * 目标类，此类中的一些调用将会被Mock掉
 * Target class, some invocations inside this class will be mocked
 */
class CookerService {

    private fun cookSandwich(): String {
        return "Real-Sandwich"
    }

    private fun cookHamburger(): String {
        return "Real-Hamburger"
    }

    fun prepareSandwich(): String {
        return hireSandwichCooker() + " & " + cookSandwich()
    }

    fun prepareHamburger(): String {
        return hireHamburgerCooker() + " & " + cookHamburger()
    }

    companion object {
        private fun hireSandwichCooker(): String {
            return "Real-Sandwich-Cooker"
        }

        private fun hireHamburgerCooker(): String {
            return "Real-Hamburger-Cooker"
        }
    }
}
