package com.alibaba.testable.agent.model;

/**
 * @author flin
 */
public class MethodInfo {

    private final String clazz;
    private final String name;
    private final String desc;

    public MethodInfo(String clazz, String name, String desc) {
        this.clazz = clazz;
        this.name = name;
        this.desc = desc;
    }

    public String getClazz() {
        return clazz;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        MethodInfo that = (MethodInfo)o;

        if (!clazz.equals(that.clazz)) { return false; }
        if (!name.equals(that.name)) { return false; }
        return desc.equals(that.desc);
    }

    @Override
    public int hashCode() {
        int result = clazz.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + desc.hashCode();
        return result;
    }
}
