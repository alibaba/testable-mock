package com.alibaba.testable.core.accessor;

import com.alibaba.testable.core.util.TypeUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author flin
 */
public class PrivateAccessor {

    private static final String KOTLIN_COMPANION_FIELD = "Companion";

    public static <T> T get(Object ref, String field) {
        try {
            Field declaredField = ref.getClass().getDeclaredField(field);
            declaredField.setAccessible(true);
            return (T)declaredField.get(ref);
        } catch (Exception e) {
            System.err.println("Failed to get private field \"" + field + "\": " + e.toString());
            return null;
        }
    }

    public static <T> void set(Object ref, String field, T value) {
        try {
            Field declaredField = ref.getClass().getDeclaredField(field);
            declaredField.setAccessible(true);
            declaredField.set(ref, value);
        } catch (Exception e) {
            System.err.println("Failed to set private field \"" + field + "\": " + e.toString());
        }
    }

    public static <T> T invoke(Object ref, String method, Object... args) {
        try {
            Class<?>[] cls = TypeUtil.getClassesFromObjects(args);
            Method declaredMethod = TypeUtil.getMethodByNameAndParameterTypes(ref.getClass().getDeclaredMethods(), method, cls);
            if (declaredMethod != null) {
                declaredMethod.setAccessible(true);
                return (T)declaredMethod.invoke(ref, args);
            }
        } catch (Exception e) {
            System.err.println("Failed to invoke private method \"" + method + "\": " + e.toString());
            return null;
        }
        return null;
    }

    public static <T> T getStatic(Class<?> clazz, String field) {
        try {
            Field declaredField = clazz.getDeclaredField(field);
            declaredField.setAccessible(true);
            return (T)declaredField.get(null);
        } catch (Exception e) {
            System.err.println("Failed to get private static field \"" + field + "\": " + e.toString());
            return null;
        }
    }

    public static <T> void setStatic(Class<?> clazz, String field, T value) {
        try {
            Field declaredField = clazz.getDeclaredField(field);
            declaredField.setAccessible(true);
            declaredField.set(null, value);
        } catch (Exception e) {
            System.err.println("Failed to set private static field \"" + field + "\": " + e.toString());
        }
    }

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
        } catch (Exception e) {
            System.err.println("Failed to invoke private static method \"" + method + "\": " + e.toString());
            return null;
        }
        return null;
    }
}
