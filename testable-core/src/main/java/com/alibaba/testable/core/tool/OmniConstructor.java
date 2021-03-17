package com.alibaba.testable.core.tool;

import com.alibaba.testable.core.model.Null;
import com.alibaba.testable.core.util.TypeUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * @author flin
 */
public class OmniConstructor {

    public static <T> T newInstance(Class<T> clazz) {
        return newInstance(clazz, isJavaAgentEnabled(clazz));
    }

    public static <T> void appendInstance(Object target, Class<T> clazz) {
        return;
    }

    public static <T> void appendInstance(Object target, int count, Class<T> clazz) {
        return;
    }

    private static boolean isJavaAgentEnabled(Class<?> clazz) {
        try {
            clazz.getDeclaredConstructor(Null.class);
        } catch (NoSuchMethodException e) {
            return false;
        }
        return true;
    }

    private static <T> T newInstance(Class<T> clazz, boolean withJavaAgent) {
        try {
            Constructor<?> constructor = withJavaAgent ?
                clazz.getDeclaredConstructor(Null.class) : clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object ins = withJavaAgent ? constructor.newInstance(new Null()) : constructor.newInstance();
            for (Field f : TypeUtil.getAllFields(clazz)) {
                f.setAccessible(true);
                if (!f.getType().isPrimitive()) {
                    f.set(ins, newInstance(f.getType(), withJavaAgent));
                }
            }
            return (T)ins;
        } catch (NoSuchMethodException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        } catch (InstantiationException e) {
            return null;
        } catch (InvocationTargetException e) {
            return null;
        }
    }

}
