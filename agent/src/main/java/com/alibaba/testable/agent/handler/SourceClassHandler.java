package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.constant.ConstPool;
import com.alibaba.testable.agent.model.MethodInfo;
import com.alibaba.testable.agent.util.ClassUtil;
import com.alibaba.testable.agent.util.CollectionUtil;
import com.alibaba.testable.agent.util.StringUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author flin
 */
public class SourceClassHandler extends BaseClassHandler {

    private final List<MethodInfo> injectMethods;

    public SourceClassHandler(List<MethodInfo> injectMethods) {
        this.injectMethods = injectMethods;
    }

    /**
     * Handle bytecode of source class
     * @param cn original class node
     */
    @Override
    protected void transform(ClassNode cn) {
        List<MethodInfo> methods = new ArrayList<MethodInfo>();
        for (MethodNode m : cn.methods) {
            if (!ConstPool.CONSTRUCTOR.equals(m.name)) {
                methods.add(new MethodInfo(cn.name, m.name, m.desc));
            }
        }
        Set<MethodInfo> memberInjectMethods = CollectionUtil.getCrossSet(methods, injectMethods);
        Set<MethodInfo> newOperatorInjectMethods = CollectionUtil.getMinusSet(injectMethods, memberInjectMethods);
        for (MethodNode m : cn.methods) {
            transformMethod(cn, m, memberInjectMethods, newOperatorInjectMethods);
        }
    }

    private void transformMethod(ClassNode cn, MethodNode mn, Set<MethodInfo> memberInjectMethods,
                                 Set<MethodInfo> newOperatorInjectMethods) {
        AbstractInsnNode[] instructions = mn.instructions.toArray();
        int i = 0;
        do {
            if (instructions[i].getOpcode() == Opcodes.INVOKESPECIAL) {
                MethodInsnNode node = (MethodInsnNode)instructions[i];
                if (cn.name.equals(node.owner) && memberInjectMethods.contains(new MethodInfo(cn.name, node.name, node.desc))) {
                    // it's a member method and an inject method for it exist
                    int rangeStart = getMemberMethodStart(instructions, i);
                    if (rangeStart >= 0) {
                        instructions = replaceMemberCallOps(cn, mn, instructions, rangeStart, i);
                        i = rangeStart;
                    }
                } else if (ConstPool.CONSTRUCTOR.equals(node.name)) {
                    // it's a new operation
                    String newOperatorInjectMethodName = getNewOperatorInjectMethodName(newOperatorInjectMethods, node);
                    if (newOperatorInjectMethodName.length() > 0) {
                        int rangeStart = getConstructorStart(instructions, node.owner, i);
                        if (rangeStart >= 0) {
                            instructions = replaceNewOps(cn, mn, newOperatorInjectMethodName, instructions, rangeStart, i);
                            i = rangeStart;
                        }
                    }
                }
            }
            i++;
        } while (i < instructions.length);
    }

    private String getNewOperatorInjectMethodName(Set<MethodInfo> newOperatorInjectMethods, MethodInsnNode node) {
        for (MethodInfo m : newOperatorInjectMethods) {
            if (m.getDesc().equals(getConstructorInjectDesc(node))) {
                return m.getName();
            }
        }
        return "";
    }

    private String getConstructorInjectDesc(MethodInsnNode constructorNode) {
        return constructorNode.desc.substring(0, constructorNode.desc.length() - 1) +
            ClassUtil.toByteCodeClassName(constructorNode.owner);
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

    private AbstractInsnNode[] replaceNewOps(ClassNode cn, MethodNode mn, String newOperatorInjectMethodName,
                                             AbstractInsnNode[] instructions, int start, int end) {
        String classType = ((TypeInsnNode)instructions[start]).desc;
        String constructorDesc = ((MethodInsnNode)instructions[end]).desc;
        String testClassName = StringUtil.getTestClassName(cn.name);
        mn.instructions.insertBefore(instructions[start], new FieldInsnNode(GETSTATIC, testClassName,
            ConstPool.TESTABLE_INJECT_REF, ClassUtil.toByteCodeClassName(testClassName)));
        mn.instructions.insertBefore(instructions[end], new MethodInsnNode(INVOKEVIRTUAL, testClassName,
            newOperatorInjectMethodName, getConstructorInjectDesc(constructorDesc, classType), false));
        mn.instructions.remove(instructions[start]);
        mn.instructions.remove(instructions[start + 1]);
        mn.instructions.remove(instructions[end]);
        return mn.instructions.toArray();
    }

    private String getConstructorInjectDesc(String constructorDesc, String classType) {
        return constructorDesc.substring(0, constructorDesc.length() - 1) +
            ClassUtil.toByteCodeClassName(classType);
    }

    private AbstractInsnNode[] replaceMemberCallOps(ClassNode cn, MethodNode mn, AbstractInsnNode[] instructions,
                                                    int start, int end) {
        MethodInsnNode method = (MethodInsnNode)instructions[end];
        String testClassName = StringUtil.getTestClassName(cn.name);
        mn.instructions.insertBefore(instructions[start], new FieldInsnNode(GETSTATIC, testClassName,
            ConstPool.TESTABLE_INJECT_REF, ClassUtil.toByteCodeClassName(testClassName)));
        mn.instructions.insertBefore(instructions[end], new MethodInsnNode(INVOKEVIRTUAL, testClassName,
            method.name, method.desc, false));
        mn.instructions.remove(instructions[start]);
        mn.instructions.remove(instructions[end]);
        return mn.instructions.toArray();
    }

}
