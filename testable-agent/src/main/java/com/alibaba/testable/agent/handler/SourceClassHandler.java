package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.constant.ConstPool;
import com.alibaba.testable.agent.model.MethodInfo;
import com.alibaba.testable.agent.util.BytecodeUtil;
import com.alibaba.testable.agent.util.ClassUtil;
import com.alibaba.testable.core.tool.TestableConst;
import com.alibaba.testable.core.util.LogUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author flin
 */
public class SourceClassHandler extends BaseClassHandler {

    private final List<MethodInfo> injectMethods;
    private final Set<Integer> invokeOps = new HashSet<Integer>() {{
        add(Opcodes.INVOKEVIRTUAL);
        add(Opcodes.INVOKESPECIAL);
        add(Opcodes.INVOKESTATIC);
        add(Opcodes.INVOKEINTERFACE);
    }};

    public SourceClassHandler(List<MethodInfo> injectMethods) {
        this.injectMethods = injectMethods;
    }

    /**
     * Handle bytecode of source class
     * @param cn original class node
     */
    @Override
    protected void transform(ClassNode cn) {
        Set<MethodInfo> memberInjectMethods = new HashSet<MethodInfo>();
        Set<MethodInfo> newOperatorInjectMethods = new HashSet<MethodInfo>();
        for (MethodInfo mi : injectMethods) {
            if (mi.getName().equals(TestableConst.CONSTRUCTOR)) {
                newOperatorInjectMethods.add(mi);
            } else {
                memberInjectMethods.add(mi);
            }
        }
        for (MethodNode m : cn.methods) {
            transformMethod(cn, m, memberInjectMethods, newOperatorInjectMethods);
        }
    }

    private void transformMethod(ClassNode cn, MethodNode mn, Set<MethodInfo> memberInjectMethods,
                                 Set<MethodInfo> newOperatorInjectMethods) {
        LogUtil.diagnose("  Handling method %s", mn.name);
        AbstractInsnNode[] instructions = mn.instructions.toArray();
        List<MethodInfo> memberInjectMethodList = new ArrayList<MethodInfo>(memberInjectMethods);
        int i = 0;
        do {
            if (invokeOps.contains(instructions[i].getOpcode())) {
                MethodInsnNode node = (MethodInsnNode)instructions[i];
                String memberInjectMethodName = getMemberInjectMethodName(memberInjectMethodList, node);
                if (memberInjectMethodName != null) {
                    // it's a member or static method and an inject method for it exist
                    int rangeStart = getMemberMethodStart(instructions, i);
                    if (rangeStart >= 0) {
                        instructions = replaceMemberCallOps(cn, mn, memberInjectMethodName, instructions,
                            node.owner, node.getOpcode(), rangeStart, i);
                        i = rangeStart;
                    }
                } else if (TestableConst.CONSTRUCTOR.equals(node.name)) {
                    // it's a new operation
                    String newOperatorInjectMethodName = getNewOperatorInjectMethodName(newOperatorInjectMethods, node);
                    if (newOperatorInjectMethodName != null) {
                        // and an inject method for it exist
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

    private String getMemberInjectMethodName(List<MethodInfo> memberInjectMethodList, MethodInsnNode node) {
        for (MethodInfo m : memberInjectMethodList) {
            String nodeOwner = ClassUtil.fitCompanionClassName(node.owner);
            if (m.getClazz().equals(nodeOwner) && m.getName().equals(node.name) && m.getDesc().equals(node.desc)) {
                return m.getMockName();
            }
        }
        return null;
    }

    private String getNewOperatorInjectMethodName(Set<MethodInfo> newOperatorInjectMethods, MethodInsnNode node) {
        for (MethodInfo m : newOperatorInjectMethods) {
            if (m.getDesc().equals(getConstructorInjectDesc(node))) {
                return m.getMockName();
            }
        }
        return null;
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
        int stackLevel = ClassUtil.getParameterTypes(((MethodInsnNode)instructions[rangeEnd]).desc).size();
        for (int i = rangeEnd - 1; i >= 0; i--) {
            switch (instructions[i].getOpcode()) {
                case Opcodes.INVOKESPECIAL:
                case Opcodes.INVOKEVIRTUAL:
                case Opcodes.INVOKEINTERFACE:
                    stackLevel += stackEffectOfInvocation(instructions[i]) + 1;
                    if (((MethodInsnNode)instructions[i]).name.equals(TestableConst.CONSTRUCTOR)) {
                        // constructor must be INVOKESPECIAL and implicitly pop 1 more stack
                        stackLevel++;
                    }
                    break;
                case Opcodes.INVOKESTATIC:
                case Opcodes.INVOKEDYNAMIC:
                    stackLevel += stackEffectOfInvocation(instructions[i]);
                    break;
                case -1:
                    // reach LineNumberNode or LabelNode
                    return i + 1;
                default:
                    stackLevel -= BytecodeUtil.stackEffect(instructions[i].getOpcode());
            }
            if (stackLevel < 0) {
                return i;
            }
        }
        return -1;
    }

    private int stackEffectOfInvocation(AbstractInsnNode instruction) {
        String desc = ((MethodInsnNode)instruction).desc;
        return ClassUtil.getParameterTypes(desc).size() - (ClassUtil.getReturnType(desc).isEmpty() ? 0 : 1);
    }

    private AbstractInsnNode[] replaceNewOps(ClassNode cn, MethodNode mn, String newOperatorInjectMethodName,
                                             AbstractInsnNode[] instructions, int start, int end) {
        LogUtil.diagnose("    Line %d, mock method %s used", getLineNum(instructions, start),
            newOperatorInjectMethodName);
        String classType = ((TypeInsnNode)instructions[start]).desc;
        String constructorDesc = ((MethodInsnNode)instructions[end]).desc;
        String testClassName = ClassUtil.getTestClassName(cn.name);
        mn.instructions.insertBefore(instructions[start], new FieldInsnNode(GETSTATIC, testClassName,
            ConstPool.TESTABLE_INJECT_REF, ClassUtil.toByteCodeClassName(testClassName)));
        mn.instructions.insertBefore(instructions[end], new MethodInsnNode(INVOKEVIRTUAL, testClassName,
            newOperatorInjectMethodName, getConstructorInjectDesc(constructorDesc, classType), false));
        mn.instructions.remove(instructions[start]);
        mn.instructions.remove(instructions[start + 1]);
        mn.instructions.remove(instructions[end]);
        return mn.instructions.toArray();
    }

    private int getLineNum(AbstractInsnNode[] instructions, int start) {
        for (int i = start - 1; i >= 0; i--) {
            if (instructions[i] instanceof LineNumberNode) {
                return ((LineNumberNode)instructions[i]).line;
            }
        }
        return 0;
    }

    private String getConstructorInjectDesc(String constructorDesc, String classType) {
        return constructorDesc.substring(0, constructorDesc.length() - 1) +
            ClassUtil.toByteCodeClassName(classType);
    }

    private AbstractInsnNode[] replaceMemberCallOps(ClassNode cn, MethodNode mn, String substitutionMethod,
                                                    AbstractInsnNode[] instructions, String ownerClass,
                                                    int opcode, int start, int end) {
        LogUtil.diagnose("    Line %d, mock method %s used", getLineNum(instructions, start), substitutionMethod);
        mn.maxStack++;
        MethodInsnNode method = (MethodInsnNode)instructions[end];
        String testClassName = ClassUtil.getTestClassName(cn.name);
        mn.instructions.insertBefore(instructions[start], new FieldInsnNode(GETSTATIC, testClassName,
            ConstPool.TESTABLE_INJECT_REF, ClassUtil.toByteCodeClassName(testClassName)));
        if (Opcodes.INVOKESTATIC == opcode || isCompanionMethod(ownerClass, opcode)) {
            // append a null value if it was a static invoke or in kotlin companion class
            mn.instructions.insertBefore(instructions[start], new InsnNode(ACONST_NULL));
            if (ClassUtil.isCompanionClassName(ownerClass)) {
                // for kotlin companion class, remove the byte code of reference to "companion" static field
                mn.instructions.remove(instructions[end - 1]);
            }
        }
        // method with @TestableMock will be modified as public access, so INVOKEVIRTUAL is used
        mn.instructions.insertBefore(instructions[end], new MethodInsnNode(INVOKEVIRTUAL, testClassName,
            substitutionMethod, addFirstParameter(method.desc, ClassUtil.fitCompanionClassName(ownerClass)), false));
        mn.instructions.remove(instructions[end]);
        return mn.instructions.toArray();
    }

    private boolean isCompanionMethod(String ownerClass, int opcode) {
        return Opcodes.INVOKEVIRTUAL == opcode && ClassUtil.isCompanionClassName(ownerClass);
    }

    private String addFirstParameter(String desc, String ownerClass) {
        return "(" + ClassUtil.toByteCodeClassName(ownerClass) + desc.substring(1);
    }

}
