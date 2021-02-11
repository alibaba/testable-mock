package com.alibaba.testable.agent.handler;

import com.alibaba.testable.core.util.InvokeRecordUtil;
import com.alibaba.testable.core.util.TestableUtil;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author flin
 */
public class TestClassHandler extends BaseClassWithContextHandler {

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
            handleInstruction(cn, mn);
        }
    }

    private void handleInstruction(ClassNode cn, MethodNode mn) {
        AbstractInsnNode[] instructions = mn.instructions.toArray();
        for (int i = 0; i < instructions.length; i++) {
            instructions = handleTestableUtil(cn, mn, instructions, i);
        }
    }

}
