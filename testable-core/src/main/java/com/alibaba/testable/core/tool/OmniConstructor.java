package com.alibaba.testable.core.tool;

import com.alibaba.testable.core.exception.ClassConstructionException;
import com.alibaba.testable.core.util.LogUtil;
import com.alibaba.testable.core.util.TypeUtil;

import java.lang.reflect.*;
import java.util.*;

import static com.alibaba.testable.core.constant.ConstPool.DOLLAR;

/**
 * @author flin
 */
public class OmniConstructor {

    private static final int INITIAL_CAPACITY = 6;
    private static final int ZERO = 0;

    private OmniConstructor() {}

    /**
     * 快速创建任意指定类型的测试对象
     *
     * @param clazz 期望的对象类型
     * @return 返回新创建的对象
     */
    public static <T> T newInstance(Class<T> clazz) {
        return handleCircleReference(newInstance(clazz, new HashSet<Class<?>>(INITIAL_CAPACITY), ZERO));
    }

    /**
     * 快速创建任意指定类型的对象数组
     *
     * @param clazz 期望的对象类型
     * @param size 数组大小
     * @return 返回新创建的对象数组
     */
    public static <T> T[] newArray(Class<T> clazz, int size) {
        return (T[])handleCircleReference(newArray(clazz, size, new HashSet<Class<?>>(INITIAL_CAPACITY), ZERO));
    }

    private static <T> T newInstance(Class<T> clazz, Set<Class<?>> classPool, int level) {
        if (!clazz.equals(Void.class)) {
            LogUtil.verbose(level, "Creating %s", clazz.getName());
        }
        if (classPool.contains(clazz)) {
            return null;
        }
        classPool.add(clazz);
        try {
            if (clazz.isPrimitive()) {
                return newPrimitive(clazz);
            } else if (clazz.isArray()) {
                return (T)newArray(clazz.getComponentType(), 0, classPool, level);
            } else if (clazz.isEnum()) {
                return newEnum(clazz);
            } else if (clazz.isInterface()) {
                return newInterface(clazz);
            } else if (Modifier.isAbstract(clazz.getModifiers())) {
                return newAbstractClass(clazz);
            }
            return newObject(clazz, classPool, level);
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
        } finally {
            classPool.remove(clazz);
        }
    }

    private static <T> T newObject(Class<T> clazz, Set<Class<?>> classPool, int level)
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Object ins = createInstance(clazz, classPool, level);
        if (!TypeUtil.isBasicType(clazz)) {
            for (Field f : TypeUtil.getAllFields(clazz)) {
                f.setAccessible(true);
                // skip "$jacocoData" field added by jacoco
                if (f.get(ins) == null && !f.getName().startsWith(DOLLAR)) {
                    f.set(ins, newInstance(f.getType(), classPool, level + 1));
                }
            }
        }
        return (T)ins;
    }

    private static Object newArray(Class<?> clazz, int size, Set<Class<?>> classPool, int level) {
        // primary[] cannot be cast to Object[], have to use Object instead of T[]
        Object array = Array.newInstance(clazz, size);
        for (int i = 0; i < size; i++) {
            Array.set(array, i, newInstance(clazz, classPool, level + 1));
        }
        return array;
    }

    private static <T> T newAbstractClass(Class<T> clazz) {
        return null;
    }

    private static <T> T newInterface(Class<T> clazz) {
        if (clazz.equals(List.class)) {
            return (T)Collections.emptyList();
        } else if (clazz.equals(Map.class)) {
            return (T)Collections.emptyMap();
        } else if (clazz.equals(Set.class)) {
            return (T)Collections.emptySet();
        }
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

    private static <T> T handleCircleReference(T instance) {
        try {
            if (instance.getClass().isArray()) {
                for (int i = 0; i < Array.getLength(instance); i++) {
                    handleCircleReference(Array.get(instance, i), instance.getClass().getComponentType(),
                        new HashMap<Class<?>, Object>(INITIAL_CAPACITY), ZERO);
                }
            } else {
                handleCircleReference(instance, instance.getClass(),
                    new HashMap<Class<?>, Object>(INITIAL_CAPACITY), ZERO);
            }
        } catch (IllegalAccessException e) {
            throw new ClassConstructionException("Failed to access field", e);
        }
        return instance;
    }

    private static <T> void handleCircleReference(T instance, Class<?> type, Map<Class<?>, Object> classPool, int level)
        throws IllegalAccessException {
        if (instance == null) {
            // don't travel null object
            return;
        }
        if (!type.equals(Void.class)) {
            LogUtil.verbose(level, "Verifying %s", type.getName());
        }
        classPool.put(type, instance);
        for (Field f : TypeUtil.getAllFields(type)) {
            f.setAccessible(true);
            Object fieldIns = f.get(instance);
            Class<?> fieldType = f.getType();
            if (fieldType.isArray()) {
                if (fieldIns != null) {
                    for (int i = 0; i < Array.getLength(fieldIns); i++) {
                        handleCircleReference(Array.get(fieldIns, i), fieldType.getComponentType(),
                            classPool, level + 1);
                    }
                }
            } else if (!fieldType.isPrimitive() && !TypeUtil.isBasicType(fieldType)) {
                if (fieldIns == null && classPool.containsKey(fieldType)) {
                    f.set(instance, classPool.get(fieldType));
                } else if (!classPool.containsKey(fieldType)) {
                    handleCircleReference(fieldIns, fieldType, classPool, level + 1);
                }
            }
        }
        classPool.remove(type);
    }

    private static Object createInstance(Class<?> clazz, Set<Class<?>> classPool, int level)
        throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Constructor<?> constructor = getBestConstructor(clazz);
        if (constructor == null) {
            throw new ClassConstructionException("Fail to invoke constructor of " + clazz.getName());
        }
        constructor.setAccessible(true);
        Class<?>[] types = constructor.getParameterTypes();
        if (types.length == 1 && types[0].equals(Void.class)) {
            return constructor.newInstance(OmniConstructor.newInstance(Void.class));
        } else {
            Object[] args = new Object[types.length];
            for (int i = 0; i < types.length; i++) {
                args[i] = types[i].equals(clazz) ? null : newInstance(types[i], classPool, level + 1);
            }
            return constructor.newInstance(args);
        }
    }

    private static Constructor<?> getBestConstructor(Class<?> clazz) {
        Constructor<?> bestConstructor = null;
        int minimalParametersSize = 999;
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            Class<?>[] types = constructor.getParameterTypes();
            if (types.length == 1 && types[0].equals(Void.class)) {
                return constructor;
            } else if (types.length < minimalParametersSize) {
                minimalParametersSize = types.length;
                bestConstructor = constructor;
            }
        }
        return bestConstructor;
    }

}
