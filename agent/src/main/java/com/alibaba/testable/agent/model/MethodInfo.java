package com.alibaba.testable.agent.model;

/**
 * @author flin
 */
public class MethodInfo {

    private int access;
    private String name;
    private String desc;
    private String signature;
    private String[] exceptions;

    public MethodInfo(int access, String name, String desc, String signature, String[] exceptions) {
        this.access = access;
        this.name = name;
        this.desc = desc;
        this.signature = signature;
        this.exceptions = exceptions;
    }

    public int getAccess() {
        return access;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getSignature() {
        return signature;
    }

    public String[] getExceptions() {
        return exceptions;
    }
}
