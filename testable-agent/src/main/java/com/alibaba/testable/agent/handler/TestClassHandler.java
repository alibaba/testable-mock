package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.handler.test.Framework;
import com.alibaba.testable.agent.model.TestCaseMethodType;
import com.alibaba.testable.core.util.LogUtil;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

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
    private final Framework framework;

    public TestClassHandler(Framework framework) {
        this.framework = framework;
    }

    /**
     * Handle bytecode of test class
     * @param cn original class node
     */
    @Override
    protected void transform(ClassNode cn) {
        LogUtil.diagnose("Found test class %s", cn.name);
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
