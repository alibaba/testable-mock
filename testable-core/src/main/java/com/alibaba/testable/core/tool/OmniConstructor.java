package com.alibaba.testable.core.tool;

import com.alibaba.testable.core.exception.ClassConstructionException;
import com.alibaba.testable.core.model.Null;
import com.alibaba.testable.core.util.TypeUtil;

import java.lang.reflect.*;

/**
 * @author flin
 */
public class OmniConstructor {

    private OmniConstructor() {}

    /**
     * 快速创建任意指定类型的测试对象
     *
     * @param clazz 期望的对象类型
     * @return 返回新创建的对象
     */
    public static <T> T newInstance(Class<T> clazz) {
        try {
            if (clazz.isPrimitive()) {
                return newPrimitive(clazz);
            } else if (clazz.isArray()) {
                return (T)newArray(clazz.getComponentType(), 0);
            } else if (clazz.isEnum()) {
                return newEnum(clazz);
            } else if (clazz.isInterface()) {
                return newInterface(clazz);
            } else if (Modifier.isAbstract(clazz.getModifiers())) {
                return newAbstractClass(clazz);
            }
            return newObject(clazz);
        } catch (NoSuchMethodException e) {
            throw new ClassConstructionException("Failed to find constructor", e);
        } catch (IllegalAccessException e) {
            throw new ClassConstructionException("Failed to access constructor", e);
        } catch (InvocationTargetException e) {
            throw new ClassConstructionException("Failed to invoke constructor", e);
        } catch (InstantiationException e) {
            throw new ClassConstructionException("Failed to complete construction", e);
        } catch (ClassCastException e) {
            throw new ClassConstructionException("Unexpected type", e);
        }
    }

    /**
     * 快速创建任意指定类型的对象数组
     *
     * @param clazz 期望的对象类型
     * @param size 数组大小
     * @return 返回新创建的对象数组
     */
    public static <T> T[] newArray(Class<T> clazz, int size) {
        T[] array = (T[])Array.newInstance(clazz, size);
        for (int i = 0; i < size; i++) {
            Array.set(array, i, newInstance(clazz));
        }
        return array;
    }

    private static <T> T newObject(Class<T> clazz)
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<?> constructor = getBestConstructor(clazz);
        constructor.setAccessible(true);
        Object ins = createInstance(constructor);
        for (Field f : TypeUtil.getAllFields(clazz)) {
            f.setAccessible(true);
            if (f.get(ins) == null) {
                f.set(ins, newInstance(f.getType()));
            }
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

    private static Object createInstance(Constructor<?> constructor)
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
