package com.alibaba.testable.core.util;

import java.util.*;

public class CollectionUtil {

    /**
     * Join a collection into string
     * @param collection many items with proper toString() method
     * @param joinSymbol splitter of echo items
     * @return a joined string
     */
    public static String joinToString(Collection<?> collection, String joinSymbol) {
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
    public static <T> boolean containsAny(Collection<T> collectionLeft, Collection<T> collectionRight) {
        for (T left : collectionLeft) {
            for (T right : collectionRight) {
                if (left.equals(right)) {
                    return true;
                }
            }
        }
        return false;
    }

}
