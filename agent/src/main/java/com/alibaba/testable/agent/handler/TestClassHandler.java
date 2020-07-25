package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.util.CollectionUtil;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author flin
 */
public class TestClassHandler extends ClassHandler {

    private static final List<String> testAnnotations = new ArrayList<String>();
    private static final String TESTABLE_SETUP_METHOD_NAME = "testableSetup";

    static {
        // JUnit4
        testAnnotations.add("org.junit.Test");
        // JUnit5
        testAnnotations.add("org.junit.jupiter.api.Test");
        // TestNG
        testAnnotations.add("org.testng.annotations.Test");
    }

    @Override
    protected void transform(ClassNode cn) {
        for (MethodNode m : cn.methods) {
            transformMethod(cn, m);
        }
    }

    private void transformMethod(ClassNode cn, MethodNode mn) {
        List<String> visibleAnnotationNames = new ArrayList<String>();
        for (AnnotationNode n : mn.visibleAnnotations) {
            visibleAnnotationNames.add(n.desc);
        }
        if (CollectionUtil.containsAny(visibleAnnotationNames, testAnnotations)) {
            InsnList il = new InsnList();
            il.add(new VarInsnNode(ALOAD, 0));
            il.add(new MethodInsnNode(INVOKESPECIAL, cn.name, TESTABLE_SETUP_METHOD_NAME, "()V", false));
            mn.instructions.insertBefore(mn.instructions.get(0), il);
        }
    }

}
