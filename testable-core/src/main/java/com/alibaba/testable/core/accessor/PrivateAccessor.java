package com.alibaba.testable.core.accessor;

import com.alibaba.testable.core.exception.MemberAccessException;
import com.alibaba.testable.core.util.TypeUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author flin
 */
public class PrivateAccessor {

    private static final String KOTLIN_COMPANION_FIELD = "Companion";

    public static class NoVerify {
        public static <T> T get(Object ref, String field) {
            try {
                return PrivateAccessor.get(ref, field);
            } catch (MemberAccessException e) {
                printError(e);
                return null;
            }
        }

        public static <T> void set(Object ref, String field, T value) {
            try {
                PrivateAccessor.set(ref, field, value);
            } catch (MemberAccessException e) {
                printError(e);
            }
        }

        public static <T> T invoke(Object ref, String method, Object... args) {
            try {
                return PrivateAccessor.invoke(ref, method, args);
            } catch (MemberAccessException e) {
                printError(e);
                return null;
            }
        }

        public static <T> T getStatic(Class<?> clazz, String field) {
            try {
                return PrivateAccessor.getStatic(clazz, field);
            } catch (MemberAccessException e) {
                printError(e);
                return null;
            }
        }

        public static <T> void setStatic(Class<?> clazz, String field, T value) {
            try {
                PrivateAccessor.setStatic(clazz, field, value);
            } catch (MemberAccessException e) {
                printError(e);
            }
        }

        public static <T> T invokeStatic(Class<?> clazz, String method, Object... args) {
            try {
                return PrivateAccessor.invokeStatic(clazz, method, args);
            } catch (MemberAccessException e) {
                printError(e);
                return null;
            }
        }

        private static void printError(MemberAccessException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            System.err.println(cause.toString());
        }
    }

    public static <T> T get(Object ref, String field) {
        try {
            Field declaredField = ref.getClass().getDeclaredField(field);
            declaredField.setAccessible(true);
            return (T)declaredField.get(ref);
        } catch (Exception e) {
            throw new MemberAccessException("Failed to get private field \"" + field + "\"", e);
        }
    }

    public static <T> void set(Object ref, String field, T value) {
        try {
            Field declaredField = ref.getClass().getDeclaredField(field);
            declaredField.setAccessible(true);
            declaredField.set(ref, value);
        } catch (Exception e) {
            throw new MemberAccessException("Failed to set private field \"" + field + "\"", e);
        }
    }

    public static <T> T invoke(Object ref, String method, Object... args) {
        try {
            Class<?>[] cls = TypeUtil.getClassesFromObjects(args);
            Method declaredMethod = TypeUtil.getMethodByNameAndParameterTypes(ref.getClass().getDeclaredMethods(),
                method, cls);
            if (declaredMethod != null) {
                declaredMethod.setAccessible(true);
                return (T)declaredMethod.invoke(ref, args);
            }
        } catch (Exception e) {
            throw new MemberAccessException("Failed to invoke private method \"" + method + "\"", e);
        }
        throw new MemberAccessException("Private method \"" + method + "\" not found");
    }

    public static <T> T getStatic(Class<?> clazz, String field) {
        try {
            Field declaredField = clazz.getDeclaredField(field);
            declaredField.setAccessible(true);
            return (T)declaredField.get(null);
        } catch (Exception e) {
            throw new MemberAccessException("Failed to get private static field \"" + field + "\"", e);
        }
    }

    public static <T> void setStatic(Class<?> clazz, String field, T value) {
        try {
            Field declaredField = clazz.getDeclaredField(field);
            declaredField.setAccessible(true);
            declaredField.set(null, value);
        } catch (Exception e) {
            throw new MemberAccessException("Failed to set private static field \"" + field + "\"", e);
        }
    }

    public static <T> T construct(Class<?> clazz, Object... args) {
        try {
            Constructor<?> constructor = TypeUtil.getConstructorByNameAndParameterTypes(clazz.getDeclaredConstructors(),
                TypeUtil.getClassesFromObjects(args));
            if (constructor != null) {
                constructor.setAccessible(true);
                return (T)constructor.newInstance(args);
            }
        } catch (Exception e) {
            throw new MemberAccessException("Failed to invoke private constructor of \"" + clazz.getSimpleName() + "\"", e);
        }
        throw new MemberAccessException("Private static constructor of \"" + clazz.getSimpleName() + "\" not found");
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
            throw new MemberAccessException("Failed to invoke private static method \"" + method + "\"", e);
        }
        throw new MemberAccessException("Private static method \"" + method + "\" not found");
    }
}
