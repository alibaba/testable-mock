package com.alibaba.testable.core.util;

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

}
