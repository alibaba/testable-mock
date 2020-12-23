package com.alibaba.testable.demo;

/**
 * 演示私有成员访问功能
 * Demonstrate private member access functionality
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
