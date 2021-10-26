package com.alibaba.testable.agent.handler.test;

import com.alibaba.testable.agent.model.TestCaseMethodType;
import com.alibaba.testable.agent.util.ClassUtil;
import com.alibaba.testable.agent.util.CollectionUtil;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.alibaba.testable.core.constant.ConstPool.THIS_REF;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.RETURN;

/**
 * @author flin
 */
abstract public class CommonFramework implements Framework {

    private static final String DEFAULT_CLEANUP_METHOD = "testableCleanup";

    @Override
    public boolean fit(Set<String> classAnnotations, Set<String> methodAnnotations) {
        return CollectionUtil.containsAny(methodAnnotations, getTestMethodAnnotations());
    }

    @Override
    public TestCaseMethodType checkMethodType(MethodNode mn) {
        if (mn.visibleAnnotations == null) {
            return TestCaseMethodType.OTHERS;
        }
        for (AnnotationNode an : mn.visibleAnnotations) {
            if (getTestMethodAnnotations().contains(an.desc)) {
                return TestCaseMethodType.TEST;
            } else if (an.desc.equals(getCleanupMethodAnnotation())) {
                return TestCaseMethodType.AFTER_TEST;
            }
        }
        return TestCaseMethodType.OTHERS;
    }

    @Override
    public MethodNode getCleanupMethod(String className) {
        MethodNode cleanupMethod = new MethodNode(ACC_PUBLIC, DEFAULT_CLEANUP_METHOD, "()V", null, null);
        cleanupMethod.visibleAnnotations = Collections.singletonList(new AnnotationNode(getCleanupMethodAnnotation()));
        InsnList il = new InsnList();
        LabelNode startLabel = new LabelNode(new Label());
        LabelNode endLabel = new LabelNode(new Label());
        il.add(startLabel);
        il.add(new InsnNode(RETURN));
        il.add(endLabel);
        cleanupMethod.instructions = il;
        cleanupMethod.localVariables = Collections.singletonList(new LocalVariableNode(THIS_REF,
            ClassUtil.toByteCodeClassName(className), null, startLabel, endLabel, 0));
        cleanupMethod.maxLocals = 1;
        cleanupMethod.maxStack = 0;
        return cleanupMethod;
    }

    /**
     * Get all annotations that identify test case method
     * @return list of annotation full name
     */
    public abstract List<String> getTestMethodAnnotations();

    /**
     * Get annotation that identify test cleanup method
     * @return full name of cleanup method annotation
     */
    public abstract String getCleanupMethodAnnotation();

}
