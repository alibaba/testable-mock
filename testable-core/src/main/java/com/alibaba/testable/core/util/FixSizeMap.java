package com.alibaba.testable.core.util;

import com.sun.tools.javac.util.ListBuffer;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * @author flin
 */
public class FixSizeMap<K, V> {

    private final int capacity;
    private final Map<K, V> content;
    private final Queue<K> order;

    public FixSizeMap(int size) {
        this.capacity = size;
        this.content = new HashMap<K, V>(size);
        this.order = new ListBuffer<K>();
    }

    public V get(K key) {
        return content.get(key);
    }

    public void put(K key, V value) {
        if (order.size() >= capacity) {
            content.remove(order.poll());
        }
        order.add(key);
        content.put(key, value);
    }

    public V getOrElse(K key, V elseValue) {
        V value = get(key);
        if (value == null) {
            value = elseValue;
            put(key, value);
        }
        return value;
    }

}
