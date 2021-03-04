package com.alibaba.demo.basic;

import java.util.*;

/**
 * 演示模板方法的Mock场景
 * Demonstrate scenario of mocking template method
 */
public class DemoTemplate {

    private <T> List<T> getList(T value) {
        List<T> l = new ArrayList<>();
        l.add(value);
        return l;
    }

    private <K, V> Map<K, V> getMap(K key, V value) {
        Map<K, V> m = new HashMap<>();
        m.put(key, value);
        return m;
    }

    public String singleTemplateMethod() {
        List<String> list = getList("demo");
        return list.get(0);
    }

    public String doubleTemplateMethod() {
        Map<String, String> map = getMap("hello", "testable");
        return map.get("hello");
    }

    public Set<?> newTemplateMethod() {
        Set<String> set = new HashSet<>();
        set.add("world");
        return set;
    }

}
