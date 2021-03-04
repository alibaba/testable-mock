package com.alibaba.demo.basic;

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
     * private static method without arguments
     */
    private static String privateStaticFunc() {
        return "static";
    }

    /**
     * private static method with arguments
     */
    private static String privateStaticFuncWithArgs(String str, int i) {
        return (str == null ? "null" : str) + " + " + i;
    }

    /**
     * private member method without arguments
     */
    private String privateFunc() {
        return "member";
    }

    /**
     * private member method with arguments
     */
    private String privateFuncWithArgs(List<String> list, String str, int i) {
        return list.stream().reduce((a, s) -> a + s).orElse("") + " + " + str + " + " + i;
    }

}
