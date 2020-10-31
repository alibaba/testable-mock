package com.alibaba.testable.agent.util;

import org.objectweb.asm.tree.AnnotationNode;

/**
 * @author flin
 */
public class AnnotationUtil {

    /**
     * Read value of annotation parameter
     */
    public static  <T> T getAnnotationParameter(AnnotationNode an, String key, T defaultValue, Class<T> clazz) {
        if (an.values != null) {
            for (int i = 0; i < an.values.size(); i += 2) {
                if (an.values.get(i).equals(key)) {
                    return clazz.cast(an.values.get(i + 1));
                }
            }
        }
        return defaultValue;
    }

}
