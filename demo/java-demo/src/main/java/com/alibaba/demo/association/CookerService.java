package com.alibaba.demo.association;

/**
 * 目标类，此类中的一些调用将会被Mock掉
 * Target class, some invocations inside this class will be mocked
 */
public class CookerService {

    private static String hireSandwichCooker() {
        return "Real-Sandwich-Cooker";
    }

    private static String hireHamburgerCooker() {
        return "Real-Hamburger-Cooker";
    }

    private String cookSandwich() {
        return "Real-Sandwich";
    }

    private String cookHamburger() {
        return "Real-Hamburger";
    }

    public String prepareSandwich() {
        return hireSandwichCooker() + " & " + cookSandwich();
    }

    public String prepareHamburger() {
        return hireHamburgerCooker() + " & " + cookHamburger();
    }

}
