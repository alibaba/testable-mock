package com.alibaba.demo.association

import com.alibaba.testable.core.annotation.MockInvoke
import com.alibaba.testable.core.model.MockScope

internal class CookerServiceMock {

    @MockInvoke(targetClass = CookerService::class)
    private fun cookSandwich(): String {
        return "Faked-Sandwich"
    }

    @MockInvoke(targetClass = CookerService::class, scope = MockScope.ASSOCIATED)
    private fun cookHamburger(): String {
        return "Faked-Hamburger"
    }

    @MockInvoke(targetClass = CookerService::class)
    fun hireSandwichCooker(): String {
        return "Fake-Sandwich-Cooker"
    }

    @MockInvoke(targetClass = CookerService::class, scope = MockScope.ASSOCIATED)
    fun hireHamburgerCooker(): String {
        return "Fake-Hamburger-Cooker"
    }
}
