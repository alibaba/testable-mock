package com.alibaba.testable.agent.model;

/**
 * @author flin
 */
public class MethodInfo {

    /**
     * name of the class this method belongs to (in slash-separate format)
     */
    private final String clazz;
    /**
     * name of the source method
     */
    private final String name;
    /**
     * parameter and return value of the source method
     */
    private final String desc;
    /**
     * name of the mock method
     */
    private final String mockName;
    /**
     * parameter and return value of the mock method
     */
    private final String mockDesc;

    public MethodInfo(String clazz, String name, String desc, String mockName, String mockDesc) {
        this.clazz = clazz;
        this.name = name;
        this.desc = desc;
        this.mockName = mockName;
        this.mockDesc = mockDesc;
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

    public String getMockName() {
        return mockName;
    }

    public String getMockDesc() {
        return mockDesc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        MethodInfo that = (MethodInfo)o;

        if (!clazz.equals(that.clazz)) { return false; }
        if (!name.equals(that.name)) { return false; }
        if (!desc.equals(that.desc)) { return false; }
        if (!mockName.equals(that.mockName)) { return false; }
        return mockDesc.equals(that.mockDesc);
    }

    @Override
    public int hashCode() {
        int result = clazz.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + desc.hashCode();
        result = 31 * result + mockName.hashCode();
        result = 31 * result + mockDesc.hashCode();
        return result;
    }
}
