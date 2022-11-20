package com.alibaba.demo.inherit;

/**
 * 演示使用@MockContainer注解实现Mock容器类的多重继承
 * Demonstrate multiple inherit of mock container class with @MockContainer annotation
 */
public class DemoMultipleInherit {

    private String prefix() {
        return "ori_";
    }

    private String middle() {
        return "gin";
    }

    private String suffix(int some, String more, Object[] parameters) {
        return "_al";
    }

    public String entry() {
        return prefix() + middle() + suffix(0, null, null);
    }

}
