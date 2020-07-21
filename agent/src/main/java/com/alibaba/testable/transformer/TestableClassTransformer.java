package com.alibaba.testable.transformer;

import com.alibaba.testable.model.TravelStatus;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.alibaba.testable.constant.Const.SYS_CLASSES;

/**
 * @author flin
 */
public class TestableClassTransformer implements Opcodes {

    private final ClassNode cn = new ClassNode();

    public TestableClassTransformer(String className) throws IOException {
        ClassReader cr = new ClassReader(className);
        cr.accept(cn, 0);
    }

    public byte[] getBytes() {
        transform();
        ClassWriter cw = new ClassWriter( 0);
        cn.accept(cw);
        return cw.toByteArray();
    }

    private void transform() {
        List<String> methodNames = new ArrayList<String>();
        for (MethodNode m : cn.methods) {
            if (!"<init>".equals(m.name)) {
                methodNames.add(m.name);
            }
        }
        for (MethodNode m : cn.methods) {
            transformMethod(m, methodNames);
        }
    }

    private void transformMethod(MethodNode mn, List<String> methodNames) {
        AbstractInsnNode[] instructions = mn.instructions.toArray();
        TravelStatus status = TravelStatus.INIT;
        String target = "";
        int rangeStart = 0;
        for (int i = 0; i < instructions.length; i++) {
            if (instructions[i].getOpcode() == Opcodes.NEW) {
                TypeInsnNode node = (TypeInsnNode)instructions[i];
                if (!SYS_CLASSES.contains(node.desc) && node.desc.equals("com/alibaba/testable/BlackBox")) {
                    target = node.desc;
                    status = TravelStatus.NEW_REP;
                    rangeStart = i;
                }
            }
            if (instructions[i].getOpcode() == Opcodes.INVOKESPECIAL) {
                MethodInsnNode node = (MethodInsnNode)instructions[i];
                if (methodNames.contains(node.name) && cn.name.equals(node.owner)) {
                    status = TravelStatus.MEM_REP;
                }
                if (TravelStatus.NEW_REP == status && "<init>".equals(node.name) && target.equals(node.owner)) {
                    replaceNewOps(mn, instructions, rangeStart, i);
                }
            }
        }
    }

    private void replaceNewOps(MethodNode mn, AbstractInsnNode[] instructions, int start, int end) {

        InsnList il = new InsnList();
        il.add(new LdcInsnNode(Type.getType("Lcom/alibaba/testable/BlackBox;")));
        il.add(new InsnNode(ICONST_1));
        il.add(new TypeInsnNode(ANEWARRAY, "java/lang/Object"));
        il.add(new InsnNode(DUP));
        il.add(new InsnNode(ICONST_0));
        il.add(new LdcInsnNode("something"));
        il.add(new InsnNode(AASTORE));
        il.add(new MethodInsnNode(INVOKESTATIC, "testable_internal/n/e",
            "w", "(Ljava/lang/Class;[Ljava/lang/Object;)Ljava/lang/Object;", false));
        il.add(new TypeInsnNode(CHECKCAST, "com/alibaba/testable/BlackBox"));
        mn.instructions.insert(instructions[end], il);

        for (int z = start; z <= end; z++) {
            mn.instructions.remove(instructions[z]);
        }

        mn.maxStack += 4;
    }

}
