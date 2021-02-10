package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.util.ClassUtil;
import org.objectweb.asm.tree.*;

import static com.alibaba.testable.agent.constant.ConstPool.CONSTRUCTOR;

/**
 * @author flin
 */
public class MockClassHandler extends BaseClassHandler {

    private static final String REF_INSTANCE = "_instance";

    public MockClassHandler(String className) {
        this.mockClassName = className;
    }

    @Override
    protected void transform(ClassNode cn) {
        if (wasTransformed(cn, REF_INSTANCE, ClassUtil.toByteCodeClassName(mockClassName))) {
            return;
        }
        addGetInstanceMethod(cn);
    }

    private void addGetInstanceMethod(ClassNode cn) {
        MethodNode getInstanceMethod = new MethodNode(ACC_PUBLIC | ACC_STATIC, REF_GET_INSTANCE,
            VOID_ARGS + ClassUtil.toByteCodeClassName(mockClassName), null, null);
        InsnList il = new InsnList();
        il.add(new FieldInsnNode(GETSTATIC, mockClassName, REF_INSTANCE,
            ClassUtil.toByteCodeClassName(mockClassName)));
        LabelNode label = new LabelNode();
        il.add(new JumpInsnNode(IFNONNULL, label));
        il.add(new TypeInsnNode(NEW, mockClassName));
        il.add(new InsnNode(DUP));
        il.add(new MethodInsnNode(INVOKESPECIAL, mockClassName, CONSTRUCTOR, VOID_ARGS + VOID_RES, false));
        il.add(new FieldInsnNode(PUTSTATIC, mockClassName, REF_INSTANCE, ClassUtil.toByteCodeClassName(mockClassName)));
        il.add(label);
        il.add(new FrameNode(F_SAME, 0, null, 0, null));
        il.add(new FieldInsnNode(GETSTATIC, mockClassName, REF_INSTANCE, ClassUtil.toByteCodeClassName(mockClassName)));
        il.add(new InsnNode(ARETURN));
        getInstanceMethod.instructions = il;
        getInstanceMethod.maxStack = 2;
        getInstanceMethod.maxLocals = 0;
        cn.methods.add(getInstanceMethod);
    }

}
