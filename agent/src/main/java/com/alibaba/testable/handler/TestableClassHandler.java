package com.alibaba.testable.handler;

import com.alibaba.testable.util.ClassUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.alibaba.testable.constant.Const.SYS_CLASSES;

/**
 * @author flin
 */
public class TestableClassHandler implements Opcodes {

    private static final String CONSTRUCTOR = "<init>";
    private static final String TESTABLE_NE = "n/e";
    private static final String TESTABLE_W = "w";
    private static final String TESTABLE_F = "f";
    private static final String CONSTRUCTOR_DESC_PREFIX = "(Ljava/lang/Class;";
    private static final String METHOD_DESC_PREFIX = "(Ljava/lang/Object;Ljava/lang/String;";
    private static final String OBJECT_DESC = "Ljava/lang/Object;";
    private static final String METHOD_DESC_POSTFIX = ")Ljava/lang/Object;";

    public byte[] getBytes(String className) throws IOException {
        ClassReader cr = new ClassReader(className);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);
        transform(cn);
        ClassWriter cw = new ClassWriter( 0);
        cn.accept(cw);
        return cw.toByteArray();
    }

    private void transform(ClassNode cn) {
        Set<String> methodNames = new HashSet<String>();
        for (MethodNode m : cn.methods) {
            if (!CONSTRUCTOR.equals(m.name)) {
                methodNames.add(m.name);
            }
        }
        for (MethodNode m : cn.methods) {
            transformMethod(cn, m, methodNames);
        }
    }

    private void transformMethod(ClassNode cn, MethodNode mn, Set<String> methodNames) {
        AbstractInsnNode[] instructions = mn.instructions.toArray();
        int i = 0;
        do {
            if (instructions[i].getOpcode() == Opcodes.INVOKESPECIAL) {
                MethodInsnNode node = (MethodInsnNode)instructions[i];
                if (cn.name.equals(node.owner) && methodNames.contains(node.name)) {
                    int rangeStart = getMemberMethodStart(instructions, i);
                    if (rangeStart >= 0) {
                        instructions = replaceMemberCallOps(mn, instructions, rangeStart, i);
                        i = rangeStart;
                    }
                } else if (CONSTRUCTOR.equals(node.name) && !SYS_CLASSES.contains(node.owner)) {
                    int rangeStart = getConstructorStart(instructions, node.owner, i);
                    if (rangeStart >= 0) {
                        instructions = replaceNewOps(mn, instructions, rangeStart, i);
                        i = rangeStart;
                    }
                }
            }
            i++;
        } while (i < instructions.length);
    }

    private int getConstructorStart(AbstractInsnNode[] instructions, String target, int rangeEnd) {
        for (int i = rangeEnd - 1; i >= 0; i--) {
            if (instructions[i].getOpcode() == Opcodes.NEW && ((TypeInsnNode)instructions[i]).desc.equals(target)) {
                return i;
            }
        }
        return -1;
    }

    private int getMemberMethodStart(AbstractInsnNode[] instructions, int rangeEnd) {
        for (int i = rangeEnd - 1; i >= 0; i--) {
            if (instructions[i].getOpcode() == Opcodes.ALOAD && ((VarInsnNode)instructions[i]).var == 0) {
                return i;
            }
        }
        return -1;
    }

    private AbstractInsnNode[] replaceNewOps(MethodNode mn, AbstractInsnNode[] instructions, int start, int end) {
        String classType = ((TypeInsnNode)instructions[start]).desc;
        String constructorDesc = ((MethodInsnNode)instructions[end]).desc;
        mn.instructions.insertBefore(instructions[start], new LdcInsnNode(Type.getType("L" + classType + ";")));
        InsnList il = new InsnList();
        il.add(new MethodInsnNode(INVOKESTATIC, TESTABLE_NE, TESTABLE_W,
            getConstructorSubstitutionDesc(constructorDesc), false));
        il.add(new TypeInsnNode(CHECKCAST, classType));
        mn.instructions.insertBefore(instructions[end], il);
        mn.instructions.remove(instructions[start]);
        mn.instructions.remove(instructions[start + 1]);
        mn.instructions.remove(instructions[end]);
        mn.maxStack += 1;
        return mn.instructions.toArray();
    }

    private String getConstructorSubstitutionDesc(String constructorDesc) {
        int paramCount = ClassUtil.getParameterCount(constructorDesc);
        return CONSTRUCTOR_DESC_PREFIX + ClassUtil.repeat(OBJECT_DESC, paramCount) + METHOD_DESC_POSTFIX;
    }

    private AbstractInsnNode[] replaceMemberCallOps(MethodNode mn, AbstractInsnNode[] instructions, int start, int end) {
        String methodDesc = ((MethodInsnNode)instructions[end]).desc;
        String returnType = ClassUtil.getReturnType(methodDesc);
        String methodName = ((MethodInsnNode)instructions[end]).name;
        mn.instructions.insert(instructions[start], new LdcInsnNode(methodName));
        InsnList il = new InsnList();
        il.add(new MethodInsnNode(INVOKESTATIC, TESTABLE_NE, TESTABLE_F,
            getMethodSubstitutionDesc(methodDesc), false));
        il.add(new TypeInsnNode(CHECKCAST, returnType));
        mn.instructions.insertBefore(instructions[end], il);
        mn.instructions.remove(instructions[end]);
        mn.maxStack += 1;
        return mn.instructions.toArray();
    }

    private String getMethodSubstitutionDesc(String methodDesc) {
        int paramCount = ClassUtil.getParameterCount(methodDesc);
        return METHOD_DESC_PREFIX + ClassUtil.repeat(OBJECT_DESC, paramCount) + METHOD_DESC_POSTFIX;
    }

}
