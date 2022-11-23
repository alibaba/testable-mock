package com.alibaba.testable.agent.util;

import com.alibaba.testable.agent.constant.ConstPool;
import com.alibaba.testable.core.model.LogLevel;
import com.alibaba.testable.core.util.LogUtil;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import static com.alibaba.testable.agent.constant.ConstPool.MOCK_DIAGNOSE;

public class DiagnoseUtil {

    public static void setupByClass(ClassNode cn) {
        AnnotationNode an = AnnotationUtil.getClassAnnotation(cn, MOCK_DIAGNOSE);
        if (an != null) {
            setupDiagnose(an, ConstPool.FIELD_VALUE);
        }
    }

    private static void setupDiagnose(AnnotationNode an, String fieldDiagnose) {
        LogLevel level = AnnotationUtil.getAnnotationParameter(an, fieldDiagnose, null, LogLevel.class);
        if (level != null) {
            LogUtil.setLevel(level);
        }
    }

}
