package com.alibaba.demo.lambda;

/**
 * @author jim
 */
public class StaticMethod {

    public static String function1(Integer i) {
        return "static" + i;
    }

    public static String function2(Integer i, Double d) {
        return "static" + i + d;
    }
}
