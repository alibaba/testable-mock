package com.alibaba.demo.association

import com.alibaba.testable.core.annotation.MockMethod
import com.alibaba.testable.core.model.MockScope

internal class CookerServiceMock {

    @MockMethod(targetClass = CookerService::class)
    private fun cookSandwich(): String {
        return "Faked-Sandwich"
    }

    @MockMethod(targetClass = CookerService::class, scope = MockScope.ASSOCIATED)
    private fun cookHamburger(): String {
        return "Faked-Hamburger"
    }

    @MockMethod(targetClass = CookerService::class)
    fun hireSandwichCooker(): String {
        return "Fake-Sandwich-Cooker"
    }

    @MockMethod(targetClass = CookerService::class, scope = MockScope.ASSOCIATED)
    fun hireHamburgerCooker(): String {
        return "Fake-Hamburger-Cooker"
    }
}
