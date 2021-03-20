package com.alibaba.testable.core.tool;

public class DemoGrandChild {

    private int i = 1;

    private final long l = 1L;

    private static Integer si = 2;

    private static final Long sl = 2L;

    public int get() {
        return i;
    }

    public void set(int i) {
        this.i = i;
    }

    public Integer getStatic() {
        return si;
    }

    public void setStatic(Integer i) {
        this.si = si;
    }

}
