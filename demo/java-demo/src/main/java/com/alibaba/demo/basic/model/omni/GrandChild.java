package com.alibaba.demo.basic.model.omni;

public class GrandChild {

    // ---------- 内部成员字段 ----------

    private int value;

    private String content;

    /* ---------------------------------------
    以下Getters/Setters方法仅为便于功能演示而添加
    并非OmniConstructor或OmniAccessor功能所需
    --------------------------------------- */

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
