package com.alibaba.testable.generator.model;

public class Statement {

    private String line;
    private Object[] params;

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public Statement(String line, Object[] params) {
        this.line = line;
        this.params = params;
    }
}
