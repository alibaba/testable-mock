package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.handler.test.*;
import com.alibaba.testable.agent.model.TestCaseMethodType;
import com.alibaba.testable.core.util.LogUtil;
import org.objectweb.asm.tree.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @author flin
 */
public class TestClassHandler extends BaseClassWithContextHandler {

    private static final String METHOD_INIT = "init";
    private static final String DESC_METHOD_INIT = "()V";
    private static final String METHOD_CLEAN = "clean";
    private static final String DESC_METHOD_CLEAN = "()V";

    private int testCaseCount = 0;
    private boolean shouldGenerateCleanupMethod = true;

    private final Framework[] frameworkClasses = new Framework[] {
        new JUnit4Framework(),
        new JUnit5Framework(),
        new TestNgFramework(),
        new TestNgOnClassFramework(),
        new SpockFramework()
    };

    /**
     * Handle bytecode of test class
     * @param cn original class node
     */
    @Override
    protected void transform(ClassNode cn) {
        Framework framework = checkFramework(cn);
        if (framework == null) {
            LogUtil.warn("Failed to detect test framework for %s", cn.name);
            return;
        }
        for (MethodNode mn : cn.methods) {
            handleTestableUtil(mn);
            handleTestCaseMethod(mn, framework);
        }
        if (shouldGenerateCleanupMethod) {
            MethodNode cleanupMethod = framework.getCleanupMethod(cn.name);
            injectMockContextClean(cleanupMethod);
            cn.methods.add(cleanupMethod);
        }
        LogUtil.diagnose("  Found %d test cases", testCaseCount);
    }

    private Framework checkFramework(ClassNode cn) {
        Set<String> classAnnotationSet = new HashSet<String>();
        Set<String> methodAnnotationSet = new HashSet<String>();
        if (cn.visibleAnnotations != null) {
            for (AnnotationNode an : cn.visibleAnnotations) {
                classAnnotationSet.add(an.desc);
            }
        }
        for (MethodNode mn : cn.methods) {
            if (mn.visibleAnnotations != null) {
                for (AnnotationNode an : mn.visibleAnnotations) {
                    methodAnnotationSet.add(an.desc);
                }
            }
        }
        for (Framework i : frameworkClasses) {
            if (i.fit(classAnnotationSet, methodAnnotationSet)) {
                return i;
            }
        }
        return null;
    }

    private void handleTestCaseMethod(MethodNode mn, Framework framework) {
        TestCaseMethodType type = framework.checkMethodType(mn);
        if (type.equals(TestCaseMethodType.TEST)) {
            LogUtil.verbose("   Test case \"%s\"", mn.name);
            injectMockContextInit(mn);
            testCaseCount++;
        } else if (type.equals(TestCaseMethodType.AFTER_TEST)) {
            injectMockContextClean(mn);
            shouldGenerateCleanupMethod = false;
        }
    }

    private void injectMockContextInit(MethodNode mn) {
        InsnList il = new InsnList();
        il.add(new MethodInsnNode(INVOKESTATIC, CLASS_MOCK_CONTEXT_UTIL, METHOD_INIT, DESC_METHOD_INIT, false));
        mn.instructions.insertBefore(mn.instructions.getFirst(), il);
    }

    private void injectMockContextClean(MethodNode mn) {
        mn.instructions.insertBefore(mn.instructions.getFirst(), new MethodInsnNode(INVOKESTATIC,
            CLASS_MOCK_CONTEXT_UTIL, METHOD_CLEAN, DESC_METHOD_CLEAN, false));
    }

}
