package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.constant.ConstPool;
import com.alibaba.testable.agent.util.ClassUtil;
import com.alibaba.testable.agent.util.StringUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author flin
 */
public class SourceClassHandler extends ClassHandler {

    private static final String CONSTRUCTOR = "<init>";
    private static final String TESTABLE_NE = "n/e";
    private static final String TESTABLE_W = "w";
    private static final String TESTABLE_F = "f";
    private static final String CONSTRUCTOR_DESC_PREFIX = "(Ljava/lang/Class;";
    private static final String METHOD_DESC_PREFIX = "(Ljava/lang/Object;Ljava/lang/String;";
    private static final String OBJECT_DESC = "Ljava/lang/Object;";
    private static final String METHOD_DESC_POSTFIX = ")Ljava/lang/Object;";

    @Override
    protected void transform(ClassNode cn) {
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
                } else if (CONSTRUCTOR.equals(node.name) && !ConstPool.SYS_CLASSES.contains(node.owner)) {
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
        List<Byte> parameterTypes = ClassUtil.getParameterTypes(constructorDesc);
        InsnList il = new InsnList();
        il.add(new MethodInsnNode(INVOKESTATIC, TESTABLE_NE, TESTABLE_W,
            getConstructorSubstitutionDesc(parameterTypes.size())));
        il.add(new TypeInsnNode(CHECKCAST, classType));
        mn.instructions.insertBefore(instructions[end], il);
        mn.instructions.remove(instructions[start]);
        mn.instructions.remove(instructions[start + 1]);
        mn.instructions.remove(instructions[end]);
        mn.maxStack += 1;
        return mn.instructions.toArray();
    }

    private String getConstructorSubstitutionDesc(int parameterCount) {
        return CONSTRUCTOR_DESC_PREFIX + StringUtil.repeat(OBJECT_DESC, parameterCount) + METHOD_DESC_POSTFIX;
    }

    private AbstractInsnNode[] replaceMemberCallOps(MethodNode mn, AbstractInsnNode[] instructions, int start, int end) {
        String methodDesc = ((MethodInsnNode)instructions[end]).desc;
        String returnType = ClassUtil.getReturnType(methodDesc);
        String methodName = ((MethodInsnNode)instructions[end]).name;
        mn.instructions.insert(instructions[start], new LdcInsnNode(methodName));
        List<Byte> parameterTypes = ClassUtil.getParameterTypes(methodDesc);
        InsnList il = new InsnList();
        il.add(new MethodInsnNode(INVOKESTATIC, TESTABLE_NE, TESTABLE_F,
            getMethodSubstitutionDesc(parameterTypes.size())));
        il.add(new TypeInsnNode(CHECKCAST, returnType));
        mn.instructions.insertBefore(instructions[end], il);
        mn.instructions.remove(instructions[end]);
        mn.maxStack += 1;
        return mn.instructions.toArray();
    }

    private String getMethodSubstitutionDesc(int parameterCount) {
        return METHOD_DESC_PREFIX + StringUtil.repeat(OBJECT_DESC, parameterCount) + METHOD_DESC_POSTFIX;
    }

}
