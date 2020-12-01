package com.alibaba.testable.demo;

/**
 * @author flin
 */
public class DemoPrivateAccess {

    /**
     * a private static field
     */
    private static int staticCount;
    /**
     * a private member field
     */
    private int count;
    /**
     * a constant field
     */
    public final Double pi = 3.14;

    /**
     * private static method
     */
    private static String privateStaticFunc(String s, int i) {
        return s + " + " + i;
    }

    /**
     * private member method
     */
    private String privateFunc(String s, int i) {
        return s + " - " + i;
    }

}
