package com.alibaba.testable.agent.util;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import static com.alibaba.testable.agent.constant.ConstPool.FIELD_TARGET_CLASS;
import static com.alibaba.testable.agent.constant.ConstPool.FIELD_TARGET_CLASS_NAME;

public class MockInvokeUtil {

    public static String getTargetClassName(AnnotationNode an) {
        Type targetClass = AnnotationUtil.getAnnotationParameter(an, FIELD_TARGET_CLASS, null, Type.class);
        if (targetClass != null) {
            return targetClass.getClassName();
        }
        return AnnotationUtil.getAnnotationParameter(an, FIELD_TARGET_CLASS_NAME, null, String.class);
    }

    public static boolean hasTargetClassParameter(AnnotationNode an) {
        return AnnotationUtil.hasAnyAnnotationParameters(an, FIELD_TARGET_CLASS, FIELD_TARGET_CLASS_NAME);
    }

    public static boolean hasDuplicatedTargetClass(AnnotationNode an) {
        return AnnotationUtil.hasAllAnnotationParameters(an, FIELD_TARGET_CLASS, FIELD_TARGET_CLASS_NAME);
    }

}
