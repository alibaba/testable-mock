package com.alibaba.demo.association;

import com.alibaba.testable.core.annotation.MockInvoke;
import com.alibaba.testable.core.model.MockScope;

class CookerServiceMock {

    @MockInvoke(targetClass = CookerService.class)
    public static String hireSandwichCooker() {
        return "Fake-Sandwich-Cooker";
    }

    @MockInvoke(targetClass = CookerService.class, scope = MockScope.ASSOCIATED)
    public static String hireHamburgerCooker() {
        return "Fake-Hamburger-Cooker";
    }

    @MockInvoke(targetClass = CookerService.class)
    private String cookSandwich() {
        return "Faked-Sandwich";
    }

    @MockInvoke(targetClass = CookerService.class, scope = MockScope.ASSOCIATED)
    private String cookHamburger() {
        return "Faked-Hamburger";
    }
}
