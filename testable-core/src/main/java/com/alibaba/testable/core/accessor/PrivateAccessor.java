package com.alibaba.testable.core.accessor;

import com.alibaba.testable.core.exception.MemberAccessException;
import com.alibaba.testable.core.util.TypeUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author flin
 */
public class PrivateAccessor {

    private static final String KOTLIN_COMPANION_FIELD = "Companion";

    /**
     * 读取任意类的私有字段
     * @param ref 目标对象
     * @param field 目标字段名
     */
    public static <T> T get(Object ref, String field) {
        try {
            Field declaredField = ref.getClass().getDeclaredField(field);
            declaredField.setAccessible(true);
            return (T)declaredField.get(ref);
        } catch (IllegalAccessException e) {
            throw new MemberAccessException("Failed to access private field \"" + field + "\"", e);
        } catch (NoSuchFieldException e) {
            throw new MemberAccessException("Private field \"" + field + "\" not exist", e);
        }
    }

    /**
     * 修改任意类的私有字段（或常量字段）
     * @param ref 目标对象
     * @param field 目标字段名
     * @param value 目标值
     */
    public static <T> void set(Object ref, String field, T value) {
        try {
            Field declaredField = ref.getClass().getDeclaredField(field);
            declaredField.setAccessible(true);
            declaredField.set(ref, value);
        } catch (IllegalAccessException e) {
            throw new MemberAccessException("Failed to access private field \"" + field + "\"", e);
        } catch (NoSuchFieldException e) {
            throw new MemberAccessException("Private field \"" + field + "\" not exist", e);
        }
    }

    /**
     * 调用任意类的私有方法
     * @param ref 目标对象
     * @param method 目标方法名
     * @param args 方法参数
     */
    public static <T> T invoke(Object ref, String method, Object... args) {
        try {
            Class<?>[] cls = TypeUtil.getClassesFromObjects(args);
            Method declaredMethod = TypeUtil.getMethodByNameAndParameterTypes(ref.getClass().getDeclaredMethods(),
                method, cls);
            if (declaredMethod != null) {
                declaredMethod.setAccessible(true);
                return (T)declaredMethod.invoke(ref, args);
            }
        } catch (IllegalAccessException e) {
            throw new MemberAccessException("Failed to access private method \"" + method + "\"", e);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException)e.getTargetException();
            }
            throw new MemberAccessException("Invoke private method \"" + method + "\" failed with exception", e);
        }
        throw new MemberAccessException("Private method \"" + method + "\" not exist");
    }

    /**
     * 读取任意类的静态私有字段
     * @param clazz 目标类型
     * @param field 目标字段名
     */
    public static <T> T getStatic(Class<?> clazz, String field) {
        try {
            Field declaredField = clazz.getDeclaredField(field);
            declaredField.setAccessible(true);
            return (T)declaredField.get(null);
        } catch (IllegalAccessException e) {
            throw new MemberAccessException("Failed to access private static field \"" + field + "\"", e);
        } catch (NoSuchFieldException e) {
            throw new MemberAccessException("Private static field \"" + field + "\" not exist", e);
        }
    }

    /**
     * 修改任意类的静态私有字段（或静态常量字段）
     * @param clazz 目标类型
     * @param field 目标字段名
     * @param value 目标值
     */
    public static <T> void setStatic(Class<?> clazz, String field, T value) {
        try {
            Field declaredField = clazz.getDeclaredField(field);
            declaredField.setAccessible(true);
            declaredField.set(null, value);
        } catch (IllegalAccessException e) {
            throw new MemberAccessException("Failed to access private static field \"" + field + "\"", e);
        } catch (NoSuchFieldException e) {
            throw new MemberAccessException("Private static field \"" + field + "\" not exist", e);
        }
    }

    /**
     * 调用任意类的静态私有方法
     * @param clazz 目标类型
     * @param method 目标方法名
     * @param args 方法参数
     */
    public static <T> T invokeStatic(Class<?> clazz, String method, Object... args) {
        try {
            Class<?>[] cls = TypeUtil.getClassesFromObjects(args);
            Method declaredMethod = TypeUtil.getMethodByNameAndParameterTypes(clazz.getDeclaredMethods(), method, cls);
            if (declaredMethod != null) {
                declaredMethod.setAccessible(true);
                return (T)declaredMethod.invoke(null, args);
            }
            // fit kotlin companion object, will throw 'NoSuchFieldException' otherwise
            Field companionClassField = clazz.getDeclaredField(KOTLIN_COMPANION_FIELD);
            declaredMethod = TypeUtil.getMethodByNameAndParameterTypes(
                companionClassField.getType().getDeclaredMethods(), method, cls);
            Object companionInstance = getStatic(clazz, KOTLIN_COMPANION_FIELD);
            if (declaredMethod != null && companionInstance != null) {
                declaredMethod.setAccessible(true);
                return (T)declaredMethod.invoke(companionInstance, args);
            }
        } catch (IllegalAccessException e) {
            throw new MemberAccessException("Failed to access private static method \"" + method + "\"", e);
        } catch (NoSuchFieldException e) {
            throw new MemberAccessException("Private static method \"" + method + "\" not exist");
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException)e.getTargetException();
            }
            throw new MemberAccessException("Invoke private static method \"" + method + "\" failed with exception", e);
        }
        throw new MemberAccessException("Neither Private static method nor companion method \"" + method + "\" exist");
    }

    /**
     * 访问任意类的私有构造方法
     * @param clazz 目标类型
     * @param args 构造方法参数
     */
    public static <T> T construct(Class<?> clazz, Object... args) {
        try {
            Constructor<?> constructor = TypeUtil.getConstructorByNameAndParameterTypes(clazz.getDeclaredConstructors(),
                TypeUtil.getClassesFromObjects(args));
            if (constructor != null) {
                constructor.setAccessible(true);
                return (T)constructor.newInstance(args);
            }
        } catch (IllegalAccessException e) {
            throw new MemberAccessException("Failed to access private constructor of \"" + clazz.getSimpleName() + "\"", e);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException)e.getTargetException();
            }
            throw new MemberAccessException("Invoke private constructor of \"" + clazz.getSimpleName() + "\" failed with exception", e);
        } catch (InstantiationException e) {
            throw new MemberAccessException("Failed to instantiate object of \"" + clazz.getSimpleName() + "\"", e);
        }
        throw new MemberAccessException("Private constructor of \"" + clazz.getSimpleName() + "\" not exist");
    }
}
