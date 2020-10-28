package com.alibaba.testable.demo.model;

public class BlackBox implements Box {

    private String data;

    @Override
    public void put(String something) {
        data = something;
    }

    public BlackBox(String data) {
        this.data = data;
    }

    public String get() {
        return data;
    }

    public static BlackBox secretBox() {
        return new BlackBox("secret");
    }

}
