package com.alibaba.testable.core.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
            Object pObj = parameterObjects[i];
            cs[i] = (pObj == null) ? null : pObj.getClass();
        }
        return cs;
    }

    /**
     * get specified field from class or its parents
     * @param clazz class contains fields
     * @param field field to look for
     * @return field which match the name
     */
    public static Field getFieldByName(Class<?> clazz, String field) {
        try {
            return clazz.getDeclaredField(field);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) {
                return getFieldByName(clazz.getSuperclass(), field);
            } else {
                return null;
            }
        }
    }

    /**
     * get all field from class and its parents
     * @param clazz class contains fields
     * @return all fields available
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        Class<?> rawClass = clazz.isArray() ? clazz.getComponentType() : clazz;
        List<Field> fields = new ArrayList<Field>(Arrays.asList(rawClass.getDeclaredFields()));
        if (rawClass.getSuperclass() != null) {
            fields.addAll(getAllFields(rawClass.getSuperclass()));
        }
        return fields;
    }

    /**
     * get constructor by parameter matching
     * @param clazz class to construct
     * @param parameterTypes class to look for
     * @return constructor which match the parameter classes
     */
    public static Constructor<?> getConstructorByParameterTypes(Class<?> clazz, Class<?>[] parameterTypes) {
        Constructor<?>[] availableConstructors = clazz.getDeclaredConstructors();
        for (Constructor<?> c : availableConstructors) {
            if (typeEquals(c.getParameterTypes(), parameterTypes)) {
                return c;
            }
        }
        if (clazz.getSuperclass() != null) {
            return getConstructorByParameterTypes(clazz.getSuperclass(), parameterTypes);
        } else {
            return null;
        }
    }

    /**
     * get method by name and parameter matching
     * @param clazz class contains methods
     * @param methodName method to look for
     * @param parameterTypes class to look for
     * @return method which match the name and class
     */
    public static Method getMethodByNameAndParameterTypes(Class<?> clazz, String methodName, Class<?>[] parameterTypes) {
        Method[] availableMethods = clazz.getDeclaredMethods();
        for (Method m : availableMethods) {
            if (m.getName().equals(methodName) && typeEquals(m.getParameterTypes(), parameterTypes)) {
                return m;
            }
        }
        if (clazz.getSuperclass() != null) {
            return getMethodByNameAndParameterTypes(clazz.getSuperclass(), methodName, parameterTypes);
        } else {
            return null;
        }
    }

    /**
     * whether class is a system basic type
     * @param clazz type to check
     */
    public static boolean isBasicType(Class<?> clazz) {
        return clazz.isPrimitive() || clazz.isEnum() || clazz.equals(Integer.class) || clazz.equals(Short.class)
            || clazz.equals(Long.class) || clazz.equals(Byte.class) || clazz.equals(Character.class)
            || clazz.equals(Float.class) || clazz.equals(Double.class) || clazz.equals(Boolean.class)
            || clazz.equals(Class.class) || clazz.equals(String.class);
    }

    /**
     * type equals
     * @param classesLeft class to be compared
     * @param classesRight class to compare (item can be null)
     * @return whether all class equals
     */
    private static boolean typeEquals(Class<?>[] classesLeft, Class<?>[] classesRight) {
        if (classesLeft.length != classesRight.length) {
            return false;
        }
        for (int i = 0; i < classesLeft.length; i++) {
            if (classesRight[i] == null) {
                return !classesLeft[i].isPrimitive();
            }
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
