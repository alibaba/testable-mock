package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.model.MethodInfo;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ArrayList;

import static com.alibaba.testable.core.tool.PrivateAccessor.invoke;
import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.*;

class SourceClassHandlerTest {

    private final SourceClassHandler handler = new SourceClassHandler(new ArrayList<MethodInfo>(), "");

    @Test
    void should_get_member_method_start() {
        AbstractInsnNode[] instructions = new AbstractInsnNode[]{
            new VarInsnNode(ALOAD, 2),
            new VarInsnNode(ALOAD, 0),
            new VarInsnNode(ALOAD, 2),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "trim", "()Ljava/lang/String;", false),
            new InsnNode(ICONST_1),
            new InsnNode(ICONST_2),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "substring", "(II)Ljava/lang/String;", false),
            new MethodInsnNode(INVOKESPECIAL, "com/alibaba/testable/demo/DemoTest", "blackBox", "(Ljava/lang/String;)Lcom/alibaba/testable/demo/BlackBox;", false),
            new MethodInsnNode(INVOKEVIRTUAL, "com/alibaba/testable/demo/BlackBox", "callMe", "()Ljava/lang/String;", false),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false)
        };
        assertEquals(2, invoke(handler, "getMemberMethodStart", instructions, 3));
        assertEquals(2, invoke(handler, "getMemberMethodStart", instructions, 6));
        assertEquals(0, invoke(handler, "getMemberMethodStart", instructions, 9));
    }
}
