package com.alibaba.testable.core.util;

import java.util.Collection;
import java.util.Iterator;

public class CollectionUtil {

    public static Object[] slice(Object[] args, int pos) {
        int size = args.length - pos;
        if (size <= 0) {
            return new Object[0];
        }
        Object[] slicedArgs = new Object[size];
        System.arraycopy(args, pos, slicedArgs, 0, size);
        return slicedArgs;
    }

    public static String join(Collection<?> collection, String joinSymbol) {
        StringBuilder sb = new StringBuilder();
        for(Iterator<?> i = collection.iterator(); i.hasNext(); sb.append((String)i.next())) {
            if (sb.length() != 0) {
                sb.append(joinSymbol);
            }
        }
        return sb.toString();
    }

    public static <T> boolean contains(T[] collection, T target) {
        for (T item : collection) {
            if (target.equals(item)) {
                return true;
            }
        }
        return false;
    }
}
