package com.alibaba.testable.agent.handler.test;

import com.alibaba.testable.agent.model.TestCaseMethodType;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Set;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

public class TestNgOnClassFramework extends TestNgFramework {

    @Override
    public boolean fit(Set<String> classAnnotations, Set<String> methodAnnotations) {
        if (classAnnotations.contains(getTestAnnotation())) {
            if (methodAnnotations.contains(getTestAfterAnnotation())) {
                hasTestAfterMethod = true;
            }
            return true;
        }
        return false;
    }

    @Override
    public TestCaseMethodType checkMethodType(MethodNode mn) {
        if (mn.visibleAnnotations == null) {
            return (mn.access & ACC_PUBLIC) != 0 ? TestCaseMethodType.TEST : TestCaseMethodType.OTHERS;
        }
        for (AnnotationNode an : mn.visibleAnnotations) {
            if (an.desc.equals(getTestAfterAnnotation())) {
                return TestCaseMethodType.AFTER_TEST;
            }
        }
        return (mn.access & ACC_PUBLIC) != 0 ? TestCaseMethodType.TEST : TestCaseMethodType.OTHERS;
    }
}
