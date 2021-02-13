package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.util.ClassUtil;
import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.List;

/**
 * @author flin
 */
public class TestClassHandler extends BaseClassWithContextHandler {

    private static final String CLASS_MOCK_CONTEXT_UTIL = "com/alibaba/testable/core/util/MockContextUtil";
    private static final String METHOD_INIT = "init";
    private static final String DESC_METHOD_INIT = "(Ljava/lang/String;Ljava/lang/String;)V";
    private final List<String> testAnnotations = Arrays.asList(
        // JUnit 4
        "org.junit.Test",
        // JUnit 5
        "org.junit.jupiter.api.Test",
        // TestNG
        "org.testng.annotations.Test"
    );

    public TestClassHandler(String mockClassName) {
        this.mockClassName = mockClassName;
    }

    /**
     * Handle bytecode of test class
     * @param cn original class node
     */
    @Override
    protected void transform(ClassNode cn) {
        for (MethodNode mn : cn.methods) {
            handleTestableUtil(mn);
            handleTestCaseMethod(cn, mn);
        }
    }

    private void handleTestCaseMethod(ClassNode cn, MethodNode mn) {
        if (mn.visibleAnnotations == null) {
            return;
        }
        for (AnnotationNode an : mn.visibleAnnotations) {
            if (testAnnotations.contains(ClassUtil.toDotSeparateFullClassName(an.desc))) {
                injectMockContextInit(cn.name, mn);
            }
        }
    }

    private void injectMockContextInit(String testClassName, MethodNode mn) {
        InsnList il = new InsnList();
        il.add(new LdcInsnNode(testClassName));
        il.add(new LdcInsnNode(mn.name));
        il.add(new MethodInsnNode(INVOKESTATIC, CLASS_MOCK_CONTEXT_UTIL, METHOD_INIT, DESC_METHOD_INIT, false));
        mn.instructions.insertBefore(mn.instructions.getFirst(), il);
    }

}
