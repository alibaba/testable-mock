package com.alibaba.testable.demo;

/**
 * @author flin
 */
public class DemoPrivateAccess {

    private int count;

    public final Double pi = 3.14;

    /**
     * private method
     */
    private String privateFunc(String s, int i) {
        return s + " - " + i;
    }

    /**
     * method with private field access
     */
    public String privateFieldAccessFunc() {
        count += 2;
        return String.valueOf(count);
    }

}
