package com.alibaba.testable.agent.util;

import java.util.Collection;

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

}
