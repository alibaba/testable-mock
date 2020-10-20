package com.alibaba.testable.demo;

public class BlackBox {

    private String data;

    public BlackBox(String data) {
        this.data = data;
    }

    public String callMe() {
        return data;
    }

}
