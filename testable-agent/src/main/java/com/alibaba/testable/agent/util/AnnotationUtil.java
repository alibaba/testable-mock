package com.alibaba.testable.agent.util;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

/**
 * @author flin
 */
public class AnnotationUtil {

    /**
     * Read value of annotation parameter
     * @param <T> template of target parameter type
     * @param an annotation node
     * @param key name of parameter to look for
     * @param defaultValue value if parameter not exist
     * @param clazz type of target parameter
     * @return value of parameter
     */
    public static <T> T getAnnotationParameter(AnnotationNode an, String key, T defaultValue, Class<T> clazz) {
        if (an != null && an.values != null) {
            for (int i = 0; i < an.values.size(); i += 2) {
                if (an.values.get(i).equals(key)) {
                    if (clazz.isEnum()) {
                        // Enum type are stored as String[] in annotation parameter
                        String[] values = (String[])an.values.get(i + 1);
                        if (values == null || values.length != 2) {
                            return defaultValue;
                        }
                        Class<? extends Enum> enumClazz = (Class<? extends Enum>)clazz;
                        return (T)Enum.valueOf(enumClazz, values[1]);
                    }
                    try {
                        return clazz.cast(an.values.get(i + 1));
                    } catch (ClassCastException e) {
                        return defaultValue;
                    }
                }
            }
        }
        return defaultValue;
    }

    /**
     * Check whether annotation has any of the specified parameters
     * @param an annotation to check
     * @param keys name of parameters
     * @return yes or no
     */
    public static boolean hasAnyAnnotationParameters(AnnotationNode an, String... keys) {
        if (an != null && an.values != null) {
            for (int i = 0; i < an.values.size(); i += 2) {
                for (String key : keys) {
                    if (an.values.get(i).equals(key)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check whether annotation has all the specified parameters
     * @param an annotation to check
     * @param keys name of parameters
     * @return yes or no
     */
    public static boolean hasAllAnnotationParameters(AnnotationNode an, String... keys) {
        boolean found = false;
        if (an != null && an.values != null) {
            for (String key : keys) {
                for (int i = 0; i < an.values.size(); i += 2) {
                    if (an.values.get(i).equals(key)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Remove specified parameter from annotation
     * @param an annotation node
     * @param keys name of parameters to remove
     * @return true - success, false - not found
     */
    public static boolean removeAnnotationParameters(AnnotationNode an, String... keys) {
        if (an.values == null) {
            return false;
        }
        for (String key : keys) {
            for (int i = 0; i < an.values.size(); i += 2) {
                if (an.values.get(i).equals(key)) {
                    an.values.remove(i + 1);
                    an.values.remove(i);
                    break;
                }
            }
        }
        return false;
    }

    /**
     * Get specified annotation node from specified class, or null if the annotation not exist
     * @param cn class to explore
     * @param annotation name of annotation to look for
     * @return the annotation instance or null
     */
    public static AnnotationNode getClassAnnotation(ClassNode cn, String annotation) {
        if (cn != null && cn.visibleAnnotations != null) {
            for (AnnotationNode an : cn.visibleAnnotations) {
                if (ClassUtil.toByteCodeClassName(annotation).equals(an.desc)) {
                    return an;
                }
            }
        }
        return null;
    }
}
