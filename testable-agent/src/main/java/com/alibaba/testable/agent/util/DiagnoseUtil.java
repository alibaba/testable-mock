package com.alibaba.testable.agent.util;

import com.alibaba.testable.agent.constant.ConstPool;
import com.alibaba.testable.core.model.LogLevel;
import com.alibaba.testable.core.util.LogUtil;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import static com.alibaba.testable.agent.util.ClassUtil.toJavaStyleClassName;

public class DiagnoseUtil {

    private static final String FIELD_VALUE = "value";

    public static void setupByClass(ClassNode cn) {
        if (cn == null || cn.visibleAnnotations == null) {
            return;
        }
        for (AnnotationNode an : cn.visibleAnnotations) {
            if (toJavaStyleClassName(an.desc).equals(ConstPool.MOCK_DIAGNOSE)) {
                setupDiagnose(an, FIELD_VALUE);
            }
        }
    }

    private static void setupDiagnose(AnnotationNode an, String fieldDiagnose) {
        LogLevel level = AnnotationUtil.getAnnotationParameter(an, fieldDiagnose, null, LogLevel.class);
        if (level != null) {
            LogUtil.setLevel(level);
        }
    }

}
