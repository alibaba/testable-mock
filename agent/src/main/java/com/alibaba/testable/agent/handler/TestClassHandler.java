package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.constant.ConstPool;
import com.alibaba.testable.agent.util.ClassUtil;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author flin
 */
public class TestClassHandler extends BaseClassHandler {

    /**
     * Handle bytecode of test class
     * @param cn original class node
     */
    @Override
    protected void transform(ClassNode cn) {
        for (MethodNode m : cn.methods) {
            transformMethod(cn, m);
        }
        cn.fields.add(new FieldNode(ACC_PUBLIC | ACC_STATIC, ConstPool.TESTABLE_INJECT_REF,
            ClassUtil.toByteCodeClassName(cn.name), null, null));
    }

    private void transformMethod(ClassNode cn, MethodNode mn) {
        List<String> visibleAnnotationNames = new ArrayList<String>();
        if (mn.visibleAnnotations == null) {
            return;
        }
        for (AnnotationNode n : mn.visibleAnnotations) {
            visibleAnnotationNames.add(n.desc);
        }
        if (visibleAnnotationNames.contains(ClassUtil.toByteCodeClassName(ConstPool.TESTABLE_INJECT))) {
            mn.access &= ~ACC_PRIVATE;
            mn.access &= ~ACC_PROTECTED;
            mn.access |= ACC_PUBLIC;
        } else if (couldBeTestMethod(mn)) {
            injectTestableRef(cn, mn);
        }
    }

    private void injectTestableRef(ClassNode cn, MethodNode mn) {
        InsnList il = new InsnList();
        il.add(new VarInsnNode(ALOAD, 0));
        il.add(new FieldInsnNode(PUTSTATIC, cn.name, ConstPool.TESTABLE_INJECT_REF,
            ClassUtil.toByteCodeClassName(cn.name)));
        mn.instructions.insertBefore(mn.instructions.get(0), il);
    }

    /**
     * Different unit test framework may have different @Test annotation
     * but they should always NOT private, protected or static
     */
    private boolean couldBeTestMethod(MethodNode mn) {
        return (mn.access & (ACC_PRIVATE | ACC_PROTECTED | ACC_STATIC)) == 0 ;
    }

}
