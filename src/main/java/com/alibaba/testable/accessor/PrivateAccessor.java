package com.alibaba.testable.accessor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author flin
 */
public class PrivateAccessor {

    public static <T> T get(Object ref, String field) {
        try {
            Field declaredField = ref.getClass().getDeclaredField(field);
            declaredField.setAccessible(true);
            return (T)declaredField.get(ref);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> void set(Object ref, String field, T value) {
        try {
            Field declaredField = ref.getClass().getDeclaredField(field);
            declaredField.setAccessible(true);
            declaredField.set(ref, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> T invoke(Object ref, String method, Object... args) {
        try {
            Class[] cls = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                cls[i] = args[i].getClass();
            }
            Method declaredMethod = ref.getClass().getDeclaredMethod(method, cls);
            declaredMethod.setAccessible(true);
            return (T)declaredMethod.invoke(ref, args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
