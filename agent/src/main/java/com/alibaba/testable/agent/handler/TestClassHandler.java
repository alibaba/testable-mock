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
        if (!visibleAnnotationNames.contains(ConstPool.TESTABLE_INJECT) && couldBeTestMethod(mn)) {
            InsnList il = new InsnList();
            il.add(new VarInsnNode(ALOAD, 0));
            il.add(new FieldInsnNode(PUTSTATIC, cn.name, ConstPool.TESTABLE_INJECT_REF, ClassUtil.toByteCodeClassName(cn.name)));
            mn.instructions.insertBefore(mn.instructions.get(0), il);
        }
    }

    private boolean couldBeTestMethod(MethodNode mn) {
        return (mn.access & (ACC_PRIVATE | ACC_PROTECTED | ACC_STATIC)) == 0 ;
    }

}
