package com.alibaba.testable.core.util;

import com.alibaba.testable.core.model.Pair;

import java.util.*;

public class CollectionUtil {

    /**
     * Get slice of args[pos, args.length]
     */
    public static Object[] slice(Object[] args, int pos) {
        int size = args.length - pos;
        if (size <= 0) {
            return new Object[0];
        }
        Object[] slicedArgs = new Object[size];
        System.arraycopy(args, pos, slicedArgs, 0, size);
        return slicedArgs;
    }

    /**
     * Join a collection to string
     */
    public static String join(Collection<?> collection, String joinSymbol) {
        StringBuilder sb = new StringBuilder();
        for(Iterator<?> i = collection.iterator(); i.hasNext(); sb.append((String)i.next())) {
            if (sb.length() != 0) {
                sb.append(joinSymbol);
            }
        }
        return sb.toString();
    }

    /**
     * Check whether target exist in collection
     */
    public static <T> boolean contains(T[] collection, T target) {
        for (T item : collection) {
            if (target.equals(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create an array
     */
    public static <T> T[] arrayOf(T... items) {
        return items;
    }

    /**
     * Create a list
     */
    public static <T> List<T> listOf(T... items) {
        return Arrays.asList(items);
    }

    /**
     * Create a set
     */
    public static <T> Set<T> setOf(T... items) {
        return new HashSet<T>(Arrays.asList(items));
    }

    /**
     * Create a map
     */
    public static <K, V> Map<K, V> mapOf(Pair<K, V>... pair) {
        return mapOf(new HashMap<K, V>(pair.length), pair);
    }

    /**
     * Create an ordered map
     */
    public static <K, V> Map<K, V> orderMapOf(Pair<K, V>... pair) {
        return mapOf(new LinkedHashMap<K, V>(pair.length), pair);
    }

    private static <K, V> Map<K, V> mapOf(Map<K, V> kvs, Pair<K, V>[] pair) {
        for (Pair<K, V> p : pair) {
            kvs.put(p.getLeft(), p.getRight());
        }
        return kvs;
    }
}
