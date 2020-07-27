package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.util.ClassUtil;
import com.alibaba.testable.agent.util.CollectionUtil;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author flin
 */
public class TestClassHandler extends ClassHandler {

    private static final List<String> TEST_ANNOTATIONS = new ArrayList<String>();
    private static final String TESTABLE_SETUP_METHOD_NAME = "testableSetup";
    private static final String TESTABLE_SETUP_METHOD_DESC = "()V";

    static {
        // JUnit4
        TEST_ANNOTATIONS.add(ClassUtil.toByteCodeClassName("org.junit.Test"));
        // JUnit5
        TEST_ANNOTATIONS.add(ClassUtil.toByteCodeClassName("org.junit.jupiter.api.Test"));
        // TestNG
        TEST_ANNOTATIONS.add(ClassUtil.toByteCodeClassName("org.testng.annotations.Test"));
    }

    @Override
    protected void transform(ClassNode cn) {
        for (MethodNode m : cn.methods) {
            transformMethod(cn, m);
        }
    }

    private void transformMethod(ClassNode cn, MethodNode mn) {
        List<String> visibleAnnotationNames = new ArrayList<String>();
        if (mn.visibleAnnotations == null) {
            return;
        }
        for (AnnotationNode n : mn.visibleAnnotations) {
            visibleAnnotationNames.add(n.desc);
        }
        if (CollectionUtil.containsAny(visibleAnnotationNames, TEST_ANNOTATIONS)) {
            InsnList il = new InsnList();
            il.add(new VarInsnNode(ALOAD, 0));
            il.add(new MethodInsnNode(INVOKESPECIAL, cn.name, TESTABLE_SETUP_METHOD_NAME, TESTABLE_SETUP_METHOD_DESC));
            mn.instructions.insertBefore(mn.instructions.get(0), il);
        }
    }

}
