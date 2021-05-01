package com.alibaba.testable.core.tool;

import com.alibaba.testable.core.exception.MemberAccessException;
import com.alibaba.testable.core.util.TypeUtil;

import java.lang.reflect.*;

/**
 * @author flin
 */
public class PrivateAccessor {

    private static final String KOTLIN_COMPANION_FIELD = "Companion";

    private PrivateAccessor() {}

    /**
     * 读取任意类的私有字段
     * @param ref       目标对象
     * @param fieldName 目标字段名
     */
    public static <T> T get(Object ref, String fieldName) {
        return get(ref, ref.getClass(), fieldName);
    }

    /**
     * 修改任意类的私有字段（或常量字段）
     * @param ref       目标对象
     * @param fieldName 目标字段名
     * @param value     目标值
     */
    public static <T> void set(Object ref, String fieldName, T value) {
        set(ref, ref.getClass(), fieldName, value);
    }

    /**
     * 调用任意类的私有方法
     * @param ref    目标对象
     * @param method 目标方法名
     * @param args   方法参数
     */
    public static <T> T invoke(Object ref, String method, Object... args) {
        return invoke(ref, ref.getClass(), method, args);
    }

    /**
     * 读取任意类的静态私有字段
     * @param clazz     目标类型
     * @param fieldName 目标字段名
     */
    public static <T> T getStatic(Class<?> clazz, String fieldName) {
        return get(null, clazz, fieldName);
    }

    /**
     * 修改任意类的静态私有字段（或静态常量字段）
     * @param clazz     目标类型
     * @param fieldName 目标字段名
     * @param value     目标值
     */
    public static <T> void setStatic(Class<?> clazz, String fieldName, T value) {
        set(null, clazz, fieldName, value);
    }

    /**
     * 调用任意类的静态私有方法
     * @param clazz  目标类型
     * @param method 目标方法名
     * @param args   方法参数
     */
    public static <T> T invokeStatic(Class<?> clazz, String method, Object... args) {
        return invoke(null, clazz, method, args);
    }

    /**
     * 访问任意类的私有构造方法
     * @param clazz 目标类型
     * @param args  构造方法参数
     */
    public static <T> T construct(Class<?> clazz, Object... args) {
        try {
            Constructor<?> constructor = TypeUtil.getConstructorByParameterTypes(clazz,
                TypeUtil.getClassesFromObjects(args));
            if (constructor != null) {
                constructor.setAccessible(true);
                return (T)constructor.newInstance(args);
            }
        } catch (IllegalAccessException e) {
            throw new MemberAccessException("Failed to access private constructor of \"" +
                clazz.getSimpleName() + "\"", e);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException)e.getTargetException();
            }
            throw new MemberAccessException(
                "Invoke private constructor of \"" + clazz.getSimpleName() + "\" failed with exception", e);
        } catch (InstantiationException e) {
            throw new MemberAccessException("Failed to instantiate object of \"" + clazz.getSimpleName() + "\"", e);
        }
        throw new MemberAccessException("Private constructor of \"" + clazz.getSimpleName() + "\" not exist");
    }

    /**
     * 获取所有类型的公共父类
     */
    private static Class<?> getCommonParentClass(Class<?>[] cls) {
        if (cls.length < 2 || cls[0] == null || cls[0].isPrimitive()) {
            return null;
        }
        Class<?> commonClass = cls[0];
        for (int i = 1; i < cls.length; i++) {
            if (cls[i].isPrimitive()) {
                return null;
            } else if (cls[i] == null) {
                continue;
            }
            commonClass = getCommonClassOf(commonClass, cls[i]);
        }
        return commonClass;
    }

    /**
     * 获取两个类的公共父类
     */
    private static Class<?> getCommonClassOf(Class<?> cls1, Class<?> cls2) {
        if (cls1.isAssignableFrom(cls2)) {
            return cls1;
        } else if (cls2.isAssignableFrom(cls1)) {
            return cls2;
        } else if (cls1.getSuperclass().equals(Object.class) || cls2.getSuperclass().equals(Object.class)) {
            return Object.class;
        } else {
            return getCommonClassOf(cls1.getSuperclass(), cls2.getSuperclass());
        }
    }

    private static <T> T get(Object ref, Class<?> clazz, String fieldName) {
        try {
            Field field = TypeUtil.getFieldByName(clazz, fieldName);
            if (field == null) {
                throw new MemberAccessException("Private field \"" + fieldName + "\" not exist");
            }
            field.setAccessible(true);
            return (T)field.get(ref);
        } catch (IllegalAccessException e) {
            throw new MemberAccessException("Failed to access private field \"" + fieldName + "\"", e);
        }
    }

    private static <T> void set(Object ref, Class<?> clazz, String fieldName, T value) {
        try {
            Field field = TypeUtil.getFieldByName(clazz, fieldName);
            if (field == null) {
                throw new MemberAccessException("Private field \"" + fieldName + "\" not exist");
            }
            field.setAccessible(true);
            field.set(ref, value);
        } catch (IllegalAccessException e) {
            throw new MemberAccessException("Failed to access private field \"" + fieldName + "\"", e);
        }
    }

    private static <T> T invoke(Object ref, Class<?> clazz, String method, Object... args) {
        try {
            Class<?>[] cls = TypeUtil.getClassesFromObjects(args);
            Class<?> commonClass = getCommonParentClass(cls);
            Method declaredMethod = TypeUtil.getMethodByNameAndParameterTypes(clazz, method, cls);
            if (declaredMethod != null) {
                declaredMethod.setAccessible(true);
                return (T)declaredMethod.invoke(ref, args);
            } else if (commonClass != null) {
                Class<?> arrayType = Array.newInstance(commonClass, 0).getClass();
                declaredMethod = TypeUtil.getMethodByNameAndParameterTypes(clazz, method, new Class[] {arrayType});
                if (declaredMethod != null) {
                    declaredMethod.setAccessible(true);
                    return (T)declaredMethod.invoke(ref, new Object[] {args});
                }
            }
            if (ref == null) {
                // fit kotlin companion object, will throw 'NoSuchFieldException' otherwise
                Field companionClassField = clazz.getDeclaredField(KOTLIN_COMPANION_FIELD);
                declaredMethod = TypeUtil.getMethodByNameAndParameterTypes(companionClassField.getType(), method, cls);
                Object companionInstance = getStatic(clazz, KOTLIN_COMPANION_FIELD);
                if (declaredMethod != null && companionInstance != null) {
                    declaredMethod.setAccessible(true);
                    return (T)declaredMethod.invoke(companionInstance, args);
                }
            }
        } catch (IllegalAccessException e) {
            throw new MemberAccessException("Failed to access private method \"" + method + "\"", e);
        } catch (NoSuchFieldException e) {
            throw new MemberAccessException("Private method \"" + method + "\" not exist");
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException)e.getTargetException();
            }
            throw new MemberAccessException("Invoke private method \"" + method + "\" failed with exception", e);
        }
        throw new MemberAccessException("Private method \"" + method + "\" not exist");
    }
}
