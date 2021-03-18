package com.alibaba.demo.basic.model.omni;

public class Parent {

    // ---------- Member fields ----------

    private Child c;

    private Child[] cs;

    private Child.SubChild sc;

    // ---------- Getters and Setters ----------

    public Child getChild() {
        return c;
    }

    public void setChild(Child child) {
        this.c = child;
    }

    public Child[] getChildren() {
        return cs;
    }

    public void setChildren(Child[] children) {
        this.cs = children;
    }

    public Child.SubChild getSubChild() {
        return sc;
    }

    public void setSubChild(Child.SubChild subChild) {
        this.sc = subChild;
    }

}
