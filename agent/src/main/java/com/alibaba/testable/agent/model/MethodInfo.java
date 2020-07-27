package com.alibaba.testable.agent.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author flin
 */
public class MethodInfo {

    private String name;
    private String desc;

    public MethodInfo(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public static Set<String> descSet(Collection<MethodInfo> methodInfos) {
        Set<String> set = new HashSet<String>();
        for (MethodInfo m : methodInfos) {
            set.add(m.desc);
        }
        return set;
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
