package com.alibaba.testable.agent.model;

/**
 * @author flin
 */
public class MethodInfo {

    /**
     * name of the class this method belongs to
     */
    private final String clazz;
    /**
     * name of the method
     */
    private final String name;
    /**
     * name of the substitution method
     * Note: this field do NOT join the `equals()` or `hashCode()` calculation
     */
    private final String substitutionMethod;
    /**
     * parameter and return value of the method
     */
    private final String desc;

    public MethodInfo(String clazz, String name, String substitutionMethod, String desc) {
        this.clazz = clazz;
        this.name = name;
        this.substitutionMethod = substitutionMethod;
        this.desc = desc;
    }

    public String getClazz() {
        return clazz;
    }

    public String getName() {
        return name;
    }

    public String getSubstitutionMethod() {
        return substitutionMethod;
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
