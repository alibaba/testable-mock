package com.alibaba.testable.core.util;

import com.alibaba.testable.core.tool.PrivateAccessor;

import java.util.HashMap;

/**
 * @author flin
 */
public class UnnullableMap<K, V> extends HashMap<K, V> {

    V defaultValue;

    private UnnullableMap(V defaultValue) {
        this.defaultValue = defaultValue;
    }

    public static <K, V, T extends V> UnnullableMap<K, V> of(T defaultValue) {
        return new UnnullableMap<K, V>(defaultValue);
    }

    @Override
    public V get(Object key) {
        V value = super.get(key);
        if (value == null) {
            if (defaultValue instanceof Cloneable) {
                value = PrivateAccessor.invoke(defaultValue, "clone");
            } else {
                value = defaultValue;
            }
            super.put((K)key, value);
        }
        return value;
    }

    public V getOrElse(Object key, V elseValue) {
        V value = super.get(key);
        if (value == null) {
            value = elseValue;
            super.put((K)key, value);
        }
        return value;
    }

}
