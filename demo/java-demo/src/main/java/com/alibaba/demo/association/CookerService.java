package com.alibaba.demo.association;

/**
 * 目标类，此类中的一些调用将会被Mock掉
 * Target class, some invocations inside this class will be mocked
 */
public class CookerService {

    private static String hireSandwichCooker() {
        return "Sandwich-Cooker";
    }

    private static String hireHamburgerCooker() {
        return "Hamburger-Cooker";
    }

    private String cookSandwich() {
        return "Cooked-Sandwich";
    }

    private String cookHamburger() {
        return "Cooked-Hamburger";
    }

    public String prepareSandwich() {
        return hireSandwichCooker() + " & " + cookSandwich();
    }

    public String prepareHamburger() {
        return hireHamburgerCooker() + " & " + cookHamburger();
    }

}
