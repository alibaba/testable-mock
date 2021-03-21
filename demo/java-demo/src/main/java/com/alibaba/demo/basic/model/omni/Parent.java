package com.alibaba.demo.basic.model.omni;

public class Parent {

    // ---------- 内部成员字段 ----------

    private Child child;

    private Child[] children;

    /* ---------------------------------------
    以下Getters/Setters方法仅为便于功能演示而添加
    并非OmniConstructor或OmniAccessor功能所需
    --------------------------------------- */

    public Child getChild() {
        return child;
    }

    public void setChild(Child child) {
        this.child = child;
    }

    public Child[] getChildren() {
        return children;
    }

    public void setChildren(Child[] children) {
        this.children = children;
    }

}
