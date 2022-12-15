package com.alibaba.testable.core.tool;

import java.lang.reflect.Array;
import java.util.*;

public class CollectionTool {

    /**
     * Get slice of args[startPos, args.length]
     * @param args original item array
     * @param startPos index of the first item to keep
     * @return a new array with sliced items
     */
    public static <T> T[] slice(T[] args, int startPos) {
        return slice(args, startPos, args.length - 1);
    }

    /**
     * Get slice of args[startPos, endPos]
     * @param args original item array
     * @param startPos index of the first item to keep
     * @param endPos index of the last item to keep
     * @return a new array with sliced items
     */
    public static <T> T[] slice(T[] args, int startPos, int endPos) {
        int size = endPos - startPos + 1;
        if (size <= 0) {
            return (T[]) Array.newInstance(args.getClass().getComponentType(), 0);
        }
        T[] slicedArgs = (T[])Array.newInstance(args.getClass().getComponentType(), size);
        System.arraycopy(args, startPos, slicedArgs, 0, size);
        return slicedArgs;
    }

    /**
     * Create an array
     * @param items elements to add
     * @return array of the provided items
     */
    public static <T> T[] arrayOf(T... items) {
        return items;
    }

    /**
     * Create a mutable list
     * @param items elements to add
     * @return list of the provided items
     */
    public static <T> List<T> listOf(T... items) {
        return new ArrayList<T>(Arrays.asList(items));
    }

    /**
     * Create a mutable set
     * @param items elements to add
     * @return set of the provided items
     */
    public static <T> Set<T> setOf(T... items) {
        return new HashSet<T>(Arrays.asList(items));
    }

    /**
     * Create a mutable map
     * @param entry elements to add
     * @return map of the provided items
     */
    public static <K, V> Map<K, V> mapOf(Map.Entry<K, V>... entry) {
        return appendMap(new HashMap<K, V>(entry.length), entry);
    }

    /**
     * Create an mutable ordered map
     * @param entry elements to add
     * @return ordered map of the provided items
     */
    public static <K, V> Map<K, V> orderedMapOf(Map.Entry<K, V>... entry) {
        return appendMap(new LinkedHashMap<K, V>(entry.length), entry);
    }

    /**
     * Create a map entry
     * @param key the key
     * @param value the value
     * @return entry of provided key and value
     */
    public static <K, V> Map.Entry<K, V> entryOf(K key, V value) {
        return new AbstractMap.SimpleEntry<K, V>(key, value);
    }

    private static <K, V> Map<K, V> appendMap(Map<K, V> kvs, Map.Entry<K, V>[] entry) {
        for (Map.Entry<K, V> p : entry) {
            kvs.put(p.getKey(), p.getValue());
        }
        return kvs;
    }
}
