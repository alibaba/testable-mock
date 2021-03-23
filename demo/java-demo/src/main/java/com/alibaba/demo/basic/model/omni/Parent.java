package com.alibaba.demo.basic.model.omni;

public class Parent {

    /**
     * 我是一个虽然存在但无法正常使用的构造方法
     * This class have constructor with exception throw
     */
    public Parent() {
        throw new IllegalArgumentException();
    }

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
