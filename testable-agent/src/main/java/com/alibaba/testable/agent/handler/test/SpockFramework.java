package com.alibaba.testable.agent.handler.test;

import com.alibaba.testable.agent.model.TestCaseMethodType;
import com.alibaba.testable.agent.util.ClassUtil;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

import java.util.Collections;
import java.util.Set;

import static com.alibaba.testable.core.constant.ConstPool.THIS_REF;
import static org.objectweb.asm.Opcodes.*;

public class SpockFramework implements Framework {

    public static final String ANNOTATION_TEST = "Lorg/spockframework/runtime/model/FeatureMetadata;";
    private static final String NAME_CLEANUP = "cleanup";
    private static final String DESC_CLEANUP = "()Ljava/lang/Object;";

    @Override
    public boolean fit(Set<String> classAnnotations, Set<String> methodAnnotations) {
        return methodAnnotations.contains(ANNOTATION_TEST);
    }

    @Override
    public TestCaseMethodType checkMethodType(MethodNode mn) {
        if (NAME_CLEANUP.equals(mn.name)) {
            return TestCaseMethodType.AFTER_TEST;
        } else if (mn.visibleAnnotations == null) {
            return TestCaseMethodType.OTHERS;
        }
        for (AnnotationNode an : mn.visibleAnnotations) {
            if (ANNOTATION_TEST.equals(an.desc)) {
                return TestCaseMethodType.TEST;
            }
        }
        return TestCaseMethodType.OTHERS;
    }

    @Override
    public MethodNode getCleanupMethod(String className) {
        MethodNode cleanupMethod = new MethodNode(ACC_PRIVATE, NAME_CLEANUP, DESC_CLEANUP, null, null);
        InsnList il = new InsnList();
        LabelNode startLabel = new LabelNode(new Label());
        LabelNode endLabel = new LabelNode(new Label());
        il.add(startLabel);
        il.add(new InsnNode(ACONST_NULL));
        il.add(new InsnNode(ARETURN));
        il.add(endLabel);
        cleanupMethod.instructions = il;
        cleanupMethod.localVariables = Collections.singletonList(new LocalVariableNode(THIS_REF,
            ClassUtil.toByteCodeClassName(className), null, startLabel, endLabel, 0));
        cleanupMethod.maxLocals = 1;
        cleanupMethod.maxStack = 1;
        return cleanupMethod;
    }
}
