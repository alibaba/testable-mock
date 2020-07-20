package com.alibaba.testable.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class TypeUtil {

    /**
     * get classes of parameter objects
     */
    public static Class[] gcs(Object[] as) {
        Class[] cs = new Class[as.length];
        for (int i = 0; i < cs.length; i++) {
            cs[i] = as[i].getClass();
        }
        return cs;
    }

    /**
     * get method by name and parameter matching
     */
    public static Method gm(Method[] mds, String mn, Class[] cs) {
        for (Method m : mds) {
            if (m.getName().equals(mn) && te(m.getParameterTypes(), cs)) {
                return m;
            }
        }
        return null;
    }

    /**
     * get constructor by parameter matching
     */
    public static Constructor gc(Constructor<?>[] cons, Class[] cs) {
        for (Constructor c : cons) {
            if (te(c.getParameterTypes(), cs)) {
                return c;
            }
        }
        return null;
    }

    /**
     * type equeals
     */
    public static boolean te(Class[] c1, Class[] c2) {
        if (c1.length != c2.length) {
            return false;
        }
        for (int i = 0; i < c1.length; i++) {
            if (!c1[i].equals(c2[i]) && !fe(c1[i], c2[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * fuzzy equal
     * @param c1 fact types (can be primary type)
     * @param c2 user types
     */
    private static boolean fe(Class c1, Class c2) {
        return (c1.equals(int.class) && c2.equals(Integer.class)) ||
            (c1.equals(long.class) && c2.equals(Long.class)) ||
            (c1.equals(short.class) && c2.equals(Short.class)) ||
            (c1.equals(boolean.class) && c2.equals(Boolean.class)) ||
            (c1.equals(char.class) && c2.equals(Character.class)) ||
            (c1.equals(byte.class) && c2.equals(Byte.class)) ||
            (c1.equals(float.class) && c2.equals(Float.class)) ||
            (c1.equals(double.class) && c2.equals(Double.class));
    }

}
