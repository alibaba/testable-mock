package com.alibaba.testable.agent.model;

/**
 * @author flin
 */
public class MethodInfo {

    private final String name;
    private final String desc;

    public MethodInfo(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MethodInfo that = (MethodInfo)o;
        return name.equals(that.name) && desc.equals(that.desc);
    }

    @Override
    public int hashCode() {
        return 31 * name.hashCode() + desc.hashCode();
    }

}
