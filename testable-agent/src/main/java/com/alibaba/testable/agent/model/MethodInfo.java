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
     * name of the mock method
     */
    private final String mockName;
    /**
     * parameter and return value of the source method
     */
    private final String desc;

    public MethodInfo(String clazz, String name, String mockName, String desc) {
        this.clazz = clazz;
        this.name = name;
        this.mockName = mockName;
        this.desc = desc;
    }

    public String getClazz() {
        return clazz;
    }

    public String getName() {
        return name;
    }

    public String getMockName() {
        return mockName;
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
        if (!mockName.equals(that.mockName)) { return false; }
        return desc.equals(that.desc);
    }

    @Override
    public int hashCode() {
        int result = clazz.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + mockName.hashCode();
        result = 31 * result + desc.hashCode();
        return result;
    }
}
