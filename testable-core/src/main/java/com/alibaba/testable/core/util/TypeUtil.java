package com.alibaba.testable.core.util;

import java.lang.reflect.Method;

/**
 * @author flin
 */
public class TypeUtil {

    /**
     * get classes of parameter objects
     * @param parameterObjects objects
     * @return class of those objects
     */
    public static Class<?>[] getClassesFromObjects(Object[] parameterObjects) {
        Class<?>[] cs = new Class[parameterObjects.length];
        for (int i = 0; i < cs.length; i++) {
            cs[i] = parameterObjects[i].getClass();
        }
        return cs;
    }

    /**
     * get method by name and parameter matching
     * @param availableMethods available methods
     * @param methodName method to look for
     * @param parameterTypes class to look for
     * @return method which match the name and class
     */
    public static Method getMethodByNameAndParameterTypes(Method[] availableMethods,
                                                          String methodName,
                                                          Class<?>[] parameterTypes) {
        for (Method m : availableMethods) {
            if (m.getName().equals(methodName) && typeEquals(m.getParameterTypes(), parameterTypes)) {
                return m;
            }
        }
        return null;
    }

    /**
     * type equals
     * @param classesLeft class to be compared
     * @param classesRight class to compare
     * @return whether all class equals
     */
    private static boolean typeEquals(Class<?>[] classesLeft, Class<?>[] classesRight) {
        if (classesLeft.length != classesRight.length) {
            return false;
        }
        for (int i = 0; i < classesLeft.length; i++) {
            if (!classesLeft[i].isAssignableFrom(classesRight[i]) && !fuzzyEqual(classesLeft[i], classesRight[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * fuzzy equal
     * @param factTypes fact types (can be primary type)
     * @param userTypes user types
     * @return whether all class equals
     */
    private static boolean fuzzyEqual(Class<?> factTypes, Class<?> userTypes) {
        return (factTypes.equals(int.class) && userTypes.equals(Integer.class)) ||
            (factTypes.equals(long.class) && userTypes.equals(Long.class)) ||
            (factTypes.equals(short.class) && userTypes.equals(Short.class)) ||
            (factTypes.equals(boolean.class) && userTypes.equals(Boolean.class)) ||
            (factTypes.equals(char.class) && userTypes.equals(Character.class)) ||
            (factTypes.equals(byte.class) && userTypes.equals(Byte.class)) ||
            (factTypes.equals(float.class) && userTypes.equals(Float.class)) ||
            (factTypes.equals(double.class) && userTypes.equals(Double.class));
    }

}
