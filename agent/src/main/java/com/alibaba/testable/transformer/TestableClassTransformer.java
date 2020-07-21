package com.alibaba.testable.transformer;

import com.alibaba.testable.model.TravelStatus;
import com.alibaba.testable.util.ClassUtil;
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

    private static final String CONSTRUCTOR = "<init>";
    private static final String TESTABLE_NE = "testable_internal/n/e";
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
            if (!CONSTRUCTOR.equals(m.name)) {
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
        int i = 0;
        do {
            if (instructions[i].getOpcode() == Opcodes.NEW) {
                TypeInsnNode node = (TypeInsnNode)instructions[i];
                if (!SYS_CLASSES.contains(node.desc)) {
                    target = node.desc;
                    status = TravelStatus.NEW_REP;
                    rangeStart = i;
                }
            } else if (instructions[i].getOpcode() == Opcodes.INVOKESPECIAL) {
                MethodInsnNode node = (MethodInsnNode)instructions[i];
                if (methodNames.contains(node.name) && cn.name.equals(node.owner)) {
                    status = TravelStatus.MEM_REP;
                } else if (TravelStatus.NEW_REP == status && CONSTRUCTOR.equals(node.name) && target.equals(node.owner)) {
                    instructions = replaceNewOps(mn, instructions, rangeStart, i);
                    i = rangeStart;
                    status = TravelStatus.INIT;
                }
            }
            i++;
        } while (i < instructions.length);
    }

    private AbstractInsnNode[] replaceNewOps(MethodNode mn, AbstractInsnNode[] instructions, int start, int end) {
        String classType = ((TypeInsnNode)instructions[start]).desc;
        String paramTypes = ((MethodInsnNode)instructions[end]).desc;
        mn.instructions.insertBefore(instructions[start], new LdcInsnNode(Type.getType("L" + classType + ";")));
        InsnList il = new InsnList();
        il.add(new MethodInsnNode(INVOKESTATIC, TESTABLE_NE, "w", ClassUtil.generateTargetDesc(paramTypes), false));
        il.add(new TypeInsnNode(CHECKCAST, classType));
        mn.instructions.insertBefore(instructions[end], il);
        mn.instructions.remove(instructions[start]);
        mn.instructions.remove(instructions[start + 1]);
        mn.instructions.remove(instructions[end]);
        mn.maxStack += 1;
        return mn.instructions.toArray();
    }

}
