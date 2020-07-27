package com.alibaba.testable.agent.util;

import java.util.*;

/**
 * @author flin
 */
public class CollectionUtil {

    /**
     * Check two collection has any equaled item
     * @param collectionLeft the first collection
     * @param collectionRight the second collection
     */
    public static boolean containsAny(Collection collectionLeft, Collection collectionRight) {
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
     * Generate a list of item
     * @param items elements to add
     */
    public static <T> List<T> listOf(T... items) {
        List<T> list = new ArrayList<T>(items.length);
        Collections.addAll(list, items);
        return list;
    }

    /**
     * Get cross set of two collections
     * @param collectionLeft the first collection
     * @param collectionRight the second collection
     */
    public static <T> Set<T> getCrossSet(Collection<T> collectionLeft, Collection<T> collectionRight) {
        Set<T> crossSet = new HashSet<T>();
        for (T i : collectionLeft) {
            if (collectionRight.contains(i)) {
                crossSet.add(i);
            }
        }
        return crossSet;
    }

    /**
     * Get minus set of two collections
     * @param collectionRaw original collection
     * @param collectionMinus items to remove
     */
    public static <T> Set<T> getMinusSet(Collection<T> collectionRaw, Collection<T> collectionMinus) {
        Set<T> crossSet = new HashSet<T>();
        for (T i : collectionRaw) {
            if (!collectionMinus.contains(i)) {
                crossSet.add(i);
            }
        }
        return crossSet;
    }

}
