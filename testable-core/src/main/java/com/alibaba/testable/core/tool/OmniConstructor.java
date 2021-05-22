package com.alibaba.testable.core.tool;

import com.alibaba.testable.core.exception.ClassConstructionException;
import com.alibaba.testable.core.model.ConstructionOption;
import com.alibaba.testable.core.util.CollectionUtil;
import com.alibaba.testable.core.util.LogUtil;
import com.alibaba.testable.core.util.TypeUtil;

import java.lang.reflect.*;
import java.util.*;

import static com.alibaba.testable.core.constant.ConstPool.DOLLAR;
import static com.alibaba.testable.core.model.ConstructionOption.EXCEPT_LOOP_NESTING;

/**
 * @author flin
 */
public class OmniConstructor {

    private static final int INITIAL_CAPACITY = 6;
    private static final int FIRST = 1;

    private OmniConstructor() {}

    /**
     * 快速创建任意指定类型的测试对象
     *
     * @param clazz 期望的对象类型
     * @param options 可选参数
     * @return 返回新创建的对象
     */
    public static <T> T newInstance(Class<T> clazz, ConstructionOption... options) {
        T ins = newInstance(clazz, new HashSet<Class<?>>(INITIAL_CAPACITY));
        if (ins == null || CollectionUtil.contains(options, EXCEPT_LOOP_NESTING)) {
            return ins;
        }
        return handleCircleReference(ins);
    }

    /**
     * 快速创建任意指定类型的对象数组
     *
     * @param clazz 期望的对象类型
     * @param size 数组大小
     * @param options 可选参数
     * @return 返回新创建的对象数组
     */
    public static <T> T[] newArray(Class<T> clazz, int size, ConstructionOption... options) {
        T[] array = (T[])newArray(clazz, size, new HashSet<Class<?>>(INITIAL_CAPACITY));
        if (CollectionUtil.contains(options, EXCEPT_LOOP_NESTING)) {
            return array;
        }
        return handleCircleReference(array);
    }

    private static <T> T newInstance(Class<T> clazz, Set<Class<?>> classPool) {
        LogUtil.verbose(classPool.size() * 2, "Creating %s", clazz.getName());
        if (classPool.contains(clazz)) {
            return null;
        }
        classPool.add(clazz);
        try {
            if (clazz.isPrimitive()) {
                return newPrimitive(clazz);
            } else if (clazz.equals(Class.class)) {
                return (T)Object.class;
            } else if (clazz.isArray()) {
                return (T)newArray(clazz.getComponentType(), 0, classPool);
            } else if (clazz.isEnum()) {
                return newEnum(clazz);
            } else if (clazz.isInterface()) {
                return newInterface(clazz);
            } else if (Modifier.isAbstract(clazz.getModifiers())) {
                return newAbstractClass(clazz);
            }
            return newObject(clazz, classPool);
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

    private static <T> T newObject(Class<T> clazz, Set<Class<?>> classPool)
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Object ins = createInstance(clazz, classPool);
        if (!TypeUtil.isBasicType(clazz)) {
            for (Field f : TypeUtil.getAllFields(clazz)) {
                f.setAccessible(true);
                // skip "$jacocoData" field added by jacoco
                if (f.get(ins) == null && !f.getName().startsWith(DOLLAR)) {
                    f.set(ins, newInstance(f.getType(), classPool));
                }
            }
        }
        return (T)ins;
    }

    private static Object newArray(Class<?> clazz, int size, Set<Class<?>> classPool) {
        // primary[] cannot be cast to Object[], have to use Object instead of T[]
        Object array = Array.newInstance(clazz, size);
        for (int i = 0; i < size; i++) {
            Array.set(array, i, newInstance(clazz, classPool));
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
        LogUtil.verbose("Verifying " + instance.getClass());
        try {
            if (instance.getClass().isArray()) {
                for (int i = 0; i < Array.getLength(instance); i++) {
                    handleCircleReference(Array.get(instance, i), instance.getClass().getComponentType(),
                        new HashMap<Class<?>, Object>(INITIAL_CAPACITY));
                }
            } else {
                handleCircleReference(instance, instance.getClass(), new HashMap<Class<?>, Object>(INITIAL_CAPACITY));
            }
        } catch (IllegalAccessException e) {
            throw new ClassConstructionException("Failed to access field", e);
        }
        return instance;
    }

    private static void handleCircleReference(Object instance, Class<?> type, Map<Class<?>, Object> classPool)
        throws IllegalAccessException {
        if (instance == null) {
            // don't travel null object
            return;
        }
        classPool.put(type, instance);
        for (Field f : TypeUtil.getAllFields(type)) {
            if (f.getName().startsWith("$") || isStaticFinalField(f)) {
                // skip static-final fields and fields e.g. "$jacocoData"
                continue;
            }
            f.setAccessible(true);
            Object fieldIns = f.get(instance);
            Class<?> fieldType = f.getType();
            if (fieldType.isArray()) {
                Class<?> componentType = fieldType.getComponentType();
                if (fieldIns != null && !TypeUtil.isBasicType(componentType)) {
                    LogUtil.verbose(classPool.size() * 2, "Verifying Field(Array[%d]) %s", Array.getLength(fieldIns), f.getName());
                    handleCircleReferenceOfArrayField(fieldIns, componentType, classPool);
                }
            } else if (!TypeUtil.isBasicType(fieldType)) {
                if (fieldIns == null && classPool.containsKey(fieldType)) {
                    f.set(instance, classPool.get(fieldType));
                } else if (!classPool.containsKey(fieldType)) {
                    LogUtil.verbose(classPool.size() * 2, "Verifying Field %s", f.getName());
                    handleCircleReference(fieldIns, fieldType, classPool);
                }
            }
        }
        classPool.remove(type);
    }

    private static boolean isStaticFinalField(Field field) {
        return Modifier.isFinal(field.getModifiers()) && Modifier.isStatic(field.getModifiers());
    }

    private static void handleCircleReferenceOfArrayField(Object instance, Class<?> type, Map<Class<?>, Object> classPool)
        throws IllegalAccessException {
        if (type.isArray()) {
            for (int i = 0; i < Math.min(Array.getLength(instance), FIRST); i++) {
                Object arrayIns = Array.get(instance, i);
                if (arrayIns != null) {
                    handleCircleReferenceOfArrayField(arrayIns, arrayIns.getClass().getComponentType(), classPool);
                }
            }
        } else if (!classPool.containsKey(type)) {
            for (int i = 0; i < Math.min(Array.getLength(instance), FIRST); i++) {
                handleCircleReference(Array.get(instance, i), type, classPool);
            }
        }
    }

    private static Object createInstance(Class<?> clazz, Set<Class<?>> classPool)
        throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Constructor<?> constructor = getBestConstructor(clazz);
        if (constructor == null) {
            throw new ClassConstructionException("Fail to invoke constructor of " + clazz.getName());
        }
        constructor.setAccessible(true);
        Class<?>[] types = constructor.getParameterTypes();
        if (types.length == 1 && types[0].equals(Void.class)) {
            return constructor.newInstance(new Object[]{ null });
        } else {
            Object[] args = new Object[types.length];
            for (int i = 0; i < types.length; i++) {
                args[i] = types[i].equals(clazz) ? null : newInstance(types[i], classPool);
            }
            return constructor.newInstance(args);
        }
    }

    private static Constructor<?> getBestConstructor(Class<?> clazz) {
        Constructor<?> bestConstructor = null;
        int minimalExceptionCount = 999;
        int minimalParameterCount = 999;
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            Class<?>[] exceptionTypes = constructor.getExceptionTypes();
            if (parameterTypes.length == 1 && parameterTypes[0].equals(Void.class)) {
                return constructor;
            } else if (exceptionTypes.length < minimalExceptionCount
                || (exceptionTypes.length == minimalExceptionCount && parameterTypes.length < minimalParameterCount)) {
                minimalExceptionCount = exceptionTypes.length;
                minimalParameterCount = parameterTypes.length;
                bestConstructor = constructor;
            }
        }
        return bestConstructor;
    }

}
