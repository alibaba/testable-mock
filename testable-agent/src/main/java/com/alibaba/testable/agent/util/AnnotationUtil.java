package com.alibaba.testable.agent.util;

import com.alibaba.testable.agent.constant.ConstPool;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import static com.alibaba.testable.agent.constant.ByteCodeConst.TYPE_CLASS;

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
     * Remove specified parameter from annotation
     * @param an annotation node
     * @param key name of parameter to remove
     * @return true - success, false - not found
     */
    public static boolean removeAnnotationParameter(AnnotationNode an, String key) {
        if (an.values == null) {
            return false;
        }
        for (int i = 0; i < an.values.size(); i += 2) {
            if (an.values.get(i).equals(key)) {
                an.values.remove(i + 1);
                an.values.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Check is MockMethod annotation is used on a valid mock method
     * @param mn mock method
     * @param an MockMethod annotation
     * @return valid or not
     */
    public static boolean isValidMockMethod(MethodNode mn, AnnotationNode an) {
        Type targetClass = AnnotationUtil.getAnnotationParameter(an, ConstPool.FIELD_TARGET_CLASS, null, Type.class);
        String firstParameter = MethodUtil.getFirstParameter(mn.desc);
        return targetClass != null || firstParameter.charAt(0) == TYPE_CLASS;
    }
}
