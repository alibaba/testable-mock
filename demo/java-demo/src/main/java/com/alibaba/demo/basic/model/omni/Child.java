package com.alibaba.demo.basic.model.omni;

public class Child {

    /**
     * 我是一个私有的构造方法
     * This class have only private constructor
     */
    private Child() {
    }

    // ---------- 内部成员字段 ----------

    private GrandChild grandChild;

    private InnerChild subChild;

    /* ---------------------------------------
    以下Getters/Setters方法仅为便于功能演示而添加
    并非OmniConstructor或OmniAccessor功能所需
    --------------------------------------- */

    public GrandChild getGrandChild() {
        return grandChild;
    }

    public void setGrandChild(GrandChild grandChild) {
        this.grandChild = grandChild;
    }

    public InnerChild getSubChild() {
        return subChild;
    }

    public void setSubChild(InnerChild subChild) {
        this.subChild = subChild;
    }

    /**
     * 这是一个私有内部类
     * An private inner class
     */
    private class InnerChild {
        private String secret;
    }

}
