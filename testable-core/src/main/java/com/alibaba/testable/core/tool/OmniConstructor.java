package com.alibaba.testable.core.tool;

import com.alibaba.testable.core.model.Null;
import com.alibaba.testable.core.util.TypeUtil;

import java.lang.reflect.*;

/**
 * @author flin
 */
public class OmniConstructor {

    public static <T> T newInstance(Class<T> clazz) {
        try {
            if (clazz.isPrimitive()) {
                return newPrimitive(clazz);
            } else if (clazz.isArray()) {
                return newArray(clazz);
            } else if (clazz.isEnum()) {
                return newEnum(clazz);
            } else if (clazz.isInterface()) {
                return newInterface(clazz);
            } else if (Modifier.isAbstract(clazz.getModifiers())) {
                return newAbstractClass(clazz);
            }
            return newObject(clazz);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        } catch (InstantiationException e) {
            return null;
        } catch (InvocationTargetException e) {
            return null;
        } catch (ClassCastException e) {
            return null;
        }
    }

    private static <T> T newObject(Class<T> clazz)
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<?> constructor = getBestConstructor(clazz);
        constructor.setAccessible(true);
        Object ins = newInstance(constructor);
        for (Field f : TypeUtil.getAllFields(clazz)) {
            f.setAccessible(true);
            f.set(ins, newInstance(f.getType()));
        }
        return (T)ins;
    }

    private static <T> T newAbstractClass(Class<T> clazz) {
        return null;
    }

    private static <T> T newInterface(Class<T> clazz) {
        return null;
    }

    private static <T> T newEnum(Class<T> clazz)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        T[] constants = clazz.getEnumConstants();
        return constants.length > 0 ? constants[0] : null;
    }

    private static <T> T newArray(Class<T> clazz) {
        return (T)Array.newInstance(clazz.getComponentType(), 0);
    }

    private static <T> T newPrimitive(Class<T> clazz) {
        if (clazz.equals(int.class)) {
            return (T)Integer.valueOf(0);
        } else if (clazz.equals(char.class)) {
            return (T)Character.valueOf((char)0);
        } else if (clazz.equals(short.class)) {
            return (T)Short.valueOf((short)0);
        } else if (clazz.equals(long.class)) {
            return (T)Long.valueOf(0L);
        } else if (clazz.equals(double.class)) {
            return (T)Double.valueOf(0);
        } else if (clazz.equals(float.class)) {
            return (T)Float.valueOf(0F);
        } else if (clazz.equals(byte.class)) {
            return (T)Byte.valueOf((byte)0);
        } else if (clazz.equals(boolean.class)) {
            return (T)Boolean.valueOf(false);
        }
        return null;
    }

    private static boolean isJavaAgentEnabled(Class<?> clazz) {
        try {
            clazz.getDeclaredConstructor(Null.class);
        } catch (NoSuchMethodException e) {
            return false;
        }
        return true;
    }

    private static Object newInstance(Constructor<?> constructor)
        throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?>[] types = constructor.getParameterTypes();
        if (types.length == 1 && types[0].equals(Null.class)) {
            return constructor.newInstance(new Null());
        } else {
            Object[] args = new Object[types.length];
            for (int i = 0; i < types.length; i++) {
                args[i] = newInstance(types[i]);
            }
            return constructor.newInstance(args);
        }
    }

    private static Constructor<?> getBestConstructor(Class<?> clazz) throws NoSuchMethodException {
        Constructor<?> bestConstructor = null;
        int minimalParametersSize = 999;
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            Class<?>[] types = constructor.getParameterTypes();
            if (types.length == 1 && types[0].equals(Null.class)) {
                return constructor;
            } else if (types.length < minimalParametersSize) {
                minimalParametersSize = types.length;
                bestConstructor = constructor;
            }
        }
        return bestConstructor;
    }

}
