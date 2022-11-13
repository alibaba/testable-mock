package com.alibaba.testable.core.util;

import com.alibaba.testable.core.model.Pair;

import java.util.*;

public class CollectionUtil {

    /**
     * Get slice of args[startPos, args.length]
     * @param args original item array
     * @param startPos index of the first item to keep
     * @return a new array with sliced items
     */
    public static Object[] slice(Object[] args, int startPos) {
        return slice(args, startPos, args.length - 1);
    }

    /**
     * Get slice of args[startPos, endPos]
     * @param args original item array
     * @param startPos index of the first item to keep
     * @param endPos index of the last item to keep
     * @return a new array with sliced items
     */
    public static Object[] slice(Object[] args, int startPos, int endPos) {
        int size = endPos - startPos + 1;
        if (size <= 0) {
            return new Object[0];
        }
        Object[] slicedArgs = new Object[size];
        System.arraycopy(args, startPos, slicedArgs, 0, size);
        return slicedArgs;
    }

    /**
     * Join a collection into string
     * @param collection many items with proper toString() method
     * @param joinSymbol splitter of echo items
     * @return a joined string
     */
    public static String join(Collection<?> collection, String joinSymbol) {
        StringBuilder sb = new StringBuilder();
        for(Iterator<?> i = collection.iterator(); i.hasNext(); sb.append(i.next().toString())) {
            if (sb.length() != 0) {
                sb.append(joinSymbol);
            }
        }
        return sb.toString();
    }

    /**
     * Check whether target exist in collection
     * @param collection many items to find from
     * @param target an item to be found
     * @return whether target exist in collection
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
     * Check two collection has any equaled item
     * @param collectionLeft the first collection
     * @param collectionRight the second collection
     * @return whether any equaled item found
     */
    public static boolean containsAny(Collection<?> collectionLeft, Collection<?> collectionRight) {
        for (Object o : collectionLeft) {
            for (Object i : collectionRight) {
                if (o.equals(i)) {
                    return true;
                }
            }
        }
        return false;
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
     * Create a list
     * @param items elements to add
     * @return list of the provided items
     */
    public static <T> List<T> listOf(T... items) {
        return Arrays.asList(items);
    }

    /**
     * Generate a list of item
     * @param items elements to add
     * @return mutable list of the provided items
     */
    public static <T> List<T> mutableListOf(T... items) {
        List<T> list = new ArrayList<T>(items.length);
        Collections.addAll(list, items);
        return list;
    }

    /**
     * Create a set
     * @param items elements to add
     * @return set of the provided items
     */
    public static <T> Set<T> setOf(T... items) {
        return new HashSet<T>(Arrays.asList(items));
    }

    /**
     * Create a map
     * @param pair elements to add
     * @return map of the provided items
     */
    public static <K, V> Map<K, V> mapOf(Pair<K, V>... pair) {
        return mapOf(new HashMap<K, V>(pair.length), pair);
    }

    /**
     * Create an ordered map
     * @param pair elements to add
     * @return ordered map of the provided items
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
