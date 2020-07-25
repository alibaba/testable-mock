package com.alibaba.testable.core.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author flin
 */
public class TypeUtil {

    /**
     * Information of substitution method
     */
    public static class TestableSubstitution {
        public Class type;   // target instance type to new / method return type
        public Class[] parameterTypes; // constructor parameter types / member method parameter types
        public Object targetObject;  // object which provides substitution / object which provides substitution
        public String methodName;  // substitution method name / original member method name

        public TestableSubstitution(Class type, String methodName, Class[] parameterTypes, Object targetObject) {
            this.type = type;
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
            this.targetObject = targetObject;
        }
    }

    private static List<TestableSubstitution> mockNewPool = new ArrayList<>();
    private static List<TestableSubstitution> mockMemberPool = new ArrayList<>();

    /**
     * add item to constructor pool
     */
    public static void addToConstructorPool(TestableSubstitution substitution) {
        mockNewPool.add(substitution);
    }

    /**
     * add item to method pool
     */
    public static void addToMemberMethodPool(TestableSubstitution substitution) {
        mockMemberPool.add(substitution);
    }

    /**
     * substitution entry for new
     */
    public static <T> T wrapNew(Class<T> classType, Object... parameters) {
        Class[] cs = TypeUtil.getClassesFromObjects(parameters);
        if (!mockNewPool.isEmpty()) {
            try {
                TestableSubstitution pi = getFromConstructorPool(classType, cs);
                if (pi != null) {
                    Method m = pi.targetObject.getClass().getDeclaredMethod(pi.methodName, pi.parameterTypes);
                    m.setAccessible(true);
                    return (T)m.invoke(pi.targetObject, parameters);
                }
            } catch (Exception e) {
                return null;
            }
        }
        try {
            Constructor c = TypeUtil.getConstructorByParameterTypes(classType.getConstructors(), cs);
            if (c != null) {
                return (T)c.newInstance(parameters);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /**
     * substitution entry for member call
     */
    public static <T> T wrapCall(Object targetObject, String methodName, Object... parameters) {
        Class[] cs = TypeUtil.getClassesFromObjects(parameters);
        if (!mockMemberPool.isEmpty()) {
            try {
                TestableSubstitution pi = getFromMemberMethodPool(methodName, cs);
                if (pi != null) {
                    Method m = pi.targetObject.getClass().getDeclaredMethod(pi.methodName, pi.parameterTypes);
                    m.setAccessible(true);
                    return (T)m.invoke(pi.targetObject, parameters);
                }
            } catch (Exception e) {
                return null;
            }
        }
        try {
            Method m = TypeUtil.getMethodByNameAndParameterTypes(targetObject.getClass().getDeclaredMethods(), methodName, cs);
            if (m != null) {
                m.setAccessible(true);
                return (T)m.invoke(targetObject, parameters);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /**
     * get from method pool by key
     */
    private static TestableSubstitution getFromMemberMethodPool(String methodName, Class[] parameterTypes) {
        for (TestableSubstitution f : mockMemberPool) {
            if (f.methodName.equals(methodName) && TypeUtil.typeEquals(f.parameterTypes, parameterTypes)) {
                return f;
            }
        }
        return null;
    }

    /**
     * get from constructor pool by key
     */
    private static TestableSubstitution getFromConstructorPool(Class type, Class[] parameterTypes) {
        for (TestableSubstitution w : mockNewPool) {
            if (w.type.equals(type) && TypeUtil.typeEquals(w.parameterTypes, parameterTypes)) {
                return w;
            }
        }
        return null;
    }

    /**
     * get classes of parameter objects
     */
    public static Class[] getClassesFromObjects(Object[] parameterObjects) {
        Class[] cs = new Class[parameterObjects.length];
        for (int i = 0; i < cs.length; i++) {
            cs[i] = parameterObjects[i].getClass();
        }
        return cs;
    }

    /**
     * get method by name and parameter matching
     */
    public static Method getMethodByNameAndParameterTypes(Method[] availableMethods,
                                                          String methodName,
                                                          Class[] parameterTypes) {
        for (Method m : availableMethods) {
            if (m.getName().equals(methodName) &&
                typeEquals(m.getParameterTypes(), parameterTypes)) {
                return m;
            }
        }
        return null;
    }

    /**
     * get constructor by parameter matching
     */
    public static Constructor getConstructorByParameterTypes(Constructor<?>[] constructors,
                                                             Class[] parameterTypes) {
        for (Constructor c : constructors) {
            if (typeEquals(c.getParameterTypes(), parameterTypes)) {
                return c;
            }
        }
        return null;
    }

    /**
     * type equeals
     */
    public static boolean typeEquals(Class[] classesLeft, Class[] classesRight) {
        if (classesLeft.length != classesRight.length) {
            return false;
        }
        for (int i = 0; i < classesLeft.length; i++) {
            if (!classesLeft[i].equals(classesRight[i]) &&
                !fuzzyEqual(classesLeft[i], classesRight[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * fuzzy equal
     * @param factTypes fact types (can be primary type)
     * @param userTypes user types
     */
    private static boolean fuzzyEqual(Class factTypes, Class userTypes) {
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
