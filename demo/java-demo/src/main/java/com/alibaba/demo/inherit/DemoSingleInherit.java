package com.alibaba.demo.inherit;

/**
 * 演示使用extends关键字实现Mock容器类的继承
 * Demonstrate inherit of mock container class
 */
public class DemoSingleInherit {

    private String prefix() {
        return "re_";
    }

    private String suffix() {
        return "_al";
    }

    public String entry(String word) {
        return prefix() + word + suffix();
    }

}
