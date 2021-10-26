package com.alibaba.testable.demo;

import com.alibaba.testable.demo.model.BlackBox;

/**
 * 演示基本的Mock功能
 * Demonstrate basic mock functionality
 */
public class DemoBasic {

    /**
     * method with new operation
     */
    public String newFunc() {
        BlackBox component = new BlackBox("something");
        return component.get();
    }

    /**
     * method with member method invoke
     */
    public String outerFunc(String s) throws Exception {
        return "{ \"res\": \"" + innerFunc(s) + staticFunc() + "\"}";
    }

    /**
     * method with common method invoke
     */
    public String commonFunc() {
        return "anything".trim() + "__" + "anything".substring(1, 2) + "__" + "abc".startsWith("ab");
    }

    /**
     * method with static method invoke
     */
    public BlackBox getBox() {
        return BlackBox.secretBox();
    }

    /**
     * two methods invoke same private method
     */
    public String callerOne() {
        return callFromDifferentMethod();
    }

    public String callerTwo() {
        return callFromDifferentMethod();
    }

    private static String staticFunc() {
        return "_STATIC_TAIL";
    }

    private String innerFunc(String s) {
        return "Nothing";
    }

    private String callFromDifferentMethod() {
        return "realOne";
    }

}
