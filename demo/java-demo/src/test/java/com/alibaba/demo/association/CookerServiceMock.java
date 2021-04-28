package com.alibaba.demo.association;

import com.alibaba.testable.core.annotation.MockMethod;
import com.alibaba.testable.core.model.MockScope;

class CookerServiceMock {

    @MockMethod(targetClass = CookerService.class)
    public static String hireSandwichCooker() {
        return "Fake-Sandwich-Cooker";
    }

    @MockMethod(targetClass = CookerService.class, scope = MockScope.ASSOCIATED)
    public static String hireHamburgerCooker() {
        return "Fake-Hamburger-Cooker";
    }

    @MockMethod(targetClass = CookerService.class)
    private String cookSandwich() {
        return "Faked-Sandwich";
    }

    @MockMethod(targetClass = CookerService.class, scope = MockScope.ASSOCIATED)
    private String cookHamburger() {
        return "Faked-Hamburger";
    }
}
