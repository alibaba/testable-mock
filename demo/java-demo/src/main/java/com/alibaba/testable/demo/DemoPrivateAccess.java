package com.alibaba.testable.demo;

import java.util.List;

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
    private static String privateStaticFunc(String str, int i) {
        return str + " + " + i;
    }

    /**
     * private member method
     */
    private String privateFunc(List<String> list, String str, int i) {
        return list.stream().reduce((a, s) -> a + s).orElse("") + " + " + str + " + " + i;
    }

}
