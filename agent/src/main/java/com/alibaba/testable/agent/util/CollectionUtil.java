package com.alibaba.testable.agent.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author flin
 */
public class CollectionUtil {

    public static boolean containsAny(Collection hostContainer, Collection itemsToFind) {
        for (Object o : hostContainer) {
            for (Object i : itemsToFind) {
                if (o.equals(i)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static <T> List<T> listOf(T... items) {
        List<T> list = new ArrayList<T>(items.length);
        Collections.addAll(list, items);
        return list;
    }

}
