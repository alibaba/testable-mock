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
     * @return found or not
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
     * Generate a list of item
     * @param items elements to add
     * @param <T> type of element
     * @return a ArrayList of provided elements
     */
    public static <T> List<T> listOf(T... items) {
        List<T> list = new ArrayList<T>(items.length);
        Collections.addAll(list, items);
        return list;
    }

}
