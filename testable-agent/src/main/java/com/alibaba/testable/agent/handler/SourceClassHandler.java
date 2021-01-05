package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.constant.ConstPool;
import com.alibaba.testable.agent.model.MethodInfo;
import com.alibaba.testable.agent.model.ModifiedInsnNodes;
import com.alibaba.testable.agent.util.BytecodeUtil;
import com.alibaba.testable.agent.util.ClassUtil;
import com.alibaba.testable.core.util.LogUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author flin
 */
public class SourceClassHandler extends BaseClassHandler {

    private static final String TESTABLE_MARK_FIELD = "__testable";

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
        if (wasTransformed(cn, TESTABLE_MARK_FIELD, "I")) {
            return;
        }
        Set<MethodInfo> memberInjectMethods = new HashSet<MethodInfo>();
        Set<MethodInfo> newOperatorInjectMethods = new HashSet<MethodInfo>();
        for (MethodInfo im : injectMethods) {
            if (im.getName().equals(ConstPool.CONSTRUCTOR)) {
                newOperatorInjectMethods.add(im);
            } else {
                memberInjectMethods.add(im);
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
        int i = 0;
        int maxStackDiff = 0;
        do {
            if (invokeOps.contains(instructions[i].getOpcode())) {
                MethodInsnNode node = (MethodInsnNode)instructions[i];
                MethodInfo mockMethod = getMemberInjectMethodName(memberInjectMethods, node);
                if (mockMethod != null) {
                    // it's a member or static method and an inject method for it exist
                    int rangeStart = getMemberMethodStart(instructions, i);
                    if (rangeStart >= 0) {
                        ModifiedInsnNodes modifiedInsnNodes = replaceMemberCallOps(cn, mn, mockMethod,
                            instructions, node.owner, node.getOpcode(), rangeStart, i);
                        instructions = modifiedInsnNodes.nodes;
                        maxStackDiff = Math.max(maxStackDiff, modifiedInsnNodes.stackDiff);
                        i = rangeStart;
                    } else {
                        LogUtil.warn("Potential missed mocking at %s:%s", mn.name, getLineNum(instructions, i));
                    }
                } else if (ConstPool.CONSTRUCTOR.equals(node.name)) {
                    // it's a new operation
                    String newOperatorInjectMethodName = getNewOperatorInjectMethodName(newOperatorInjectMethods, node);
                    if (newOperatorInjectMethodName != null) {
                        // and an inject method for it exist
                        int rangeStart = getConstructorStart(instructions, node.owner, i);
                        if (rangeStart >= 0) {
                            ModifiedInsnNodes modifiedInsnNodes = replaceNewOps(cn, mn, newOperatorInjectMethodName,
                                instructions, rangeStart, i);
                            instructions = modifiedInsnNodes.nodes;
                            maxStackDiff = Math.max(maxStackDiff, modifiedInsnNodes.stackDiff);
                            i = rangeStart;
                        }
                    }
                }
            }
            i++;
        } while (i < instructions.length);
        mn.maxStack += maxStackDiff;
    }

    /**
     * find the mock method fit for specified method node
     * @param memberInjectMethods mock methods available
     * @param node method node to match for
     * @return mock method info
     */
    private MethodInfo getMemberInjectMethodName(Set<MethodInfo> memberInjectMethods, MethodInsnNode node) {
        for (MethodInfo m : memberInjectMethods) {
            String nodeOwner = ClassUtil.fitCompanionClassName(node.owner);
            String nodeName = ClassUtil.fitKotlinAccessorName(node.name);
            // Kotlin accessor method will append a extra type parameter
            String nodeDesc = nodeName.equals(node.name) ? node.desc : ClassUtil.removeFirstParameter(node.desc);
            if (m.getClazz().equals(nodeOwner) && m.getName().equals(nodeName) && m.getDesc().equals(nodeDesc)) {
                return m;
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
        int stackLevel = getInitialStackLevel((MethodInsnNode)instructions[rangeEnd]);
        if (stackLevel < 0) {
            return rangeEnd;
        }
        for (int i = rangeEnd - 1; i >= 0; i--) {
            stackLevel += getStackLevelChange(instructions[i]);
            if (stackLevel < 0) {
                return i;
            }
        }
        return -1;
    }

    private int getInitialStackLevel(MethodInsnNode instruction) {
        int stackLevel = ClassUtil.getParameterTypes((instruction).desc).size();
        switch (instruction.getOpcode()) {
            case Opcodes.INVOKESPECIAL:
            case Opcodes.INVOKEVIRTUAL:
            case Opcodes.INVOKEINTERFACE:
                return stackLevel;
            case Opcodes.INVOKESTATIC:
            case Opcodes.INVOKEDYNAMIC:
                return stackLevel - 1;
            default:
                return 0;
        }
    }

    private int getStackLevelChange(AbstractInsnNode instruction) {
        switch (instruction.getOpcode()) {
            case Opcodes.INVOKESPECIAL:
            case Opcodes.INVOKEVIRTUAL:
            case Opcodes.INVOKEINTERFACE:
                return stackEffectOfInvocation(instruction) + 1;
            case Opcodes.INVOKESTATIC:
            case Opcodes.INVOKEDYNAMIC:
                return stackEffectOfInvocation(instruction);
            case -1:
                // either LabelNode or LineNumberNode
                return 0;
            default:
                return -BytecodeUtil.stackEffect(instruction.getOpcode());
        }
    }

    private int stackEffectOfInvocation(AbstractInsnNode instruction) {
        String desc = ((MethodInsnNode)instruction).desc;
        return ClassUtil.getParameterTypes(desc).size() - (ClassUtil.getReturnType(desc).isEmpty() ? 0 : 1);
    }

    private ModifiedInsnNodes replaceNewOps(ClassNode cn, MethodNode mn, String newOperatorInjectMethodName,
                                             AbstractInsnNode[] instructions, int start, int end) {
        LogUtil.diagnose("    Line %d, mock method %s used", getLineNum(instructions, start),
            newOperatorInjectMethodName);
        String classType = ((TypeInsnNode)instructions[start]).desc;
        String constructorDesc = ((MethodInsnNode)instructions[end]).desc;
        String testClassName = ClassUtil.getTestClassName(cn.name);
        mn.instructions.insertBefore(instructions[end], new MethodInsnNode(INVOKESTATIC, testClassName,
            newOperatorInjectMethodName, getConstructorInjectDesc(constructorDesc, classType), false));
        mn.instructions.remove(instructions[start]);
        mn.instructions.remove(instructions[start + 1]);
        mn.instructions.remove(instructions[end]);
        return new ModifiedInsnNodes(mn.instructions.toArray(), 0);
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

    private ModifiedInsnNodes replaceMemberCallOps(ClassNode cn, MethodNode mn, MethodInfo mockMethod,
                                                   AbstractInsnNode[] instructions, String ownerClass,
                                                   int opcode, int start, int end) {
        LogUtil.diagnose("    Line %d, mock method %s used", getLineNum(instructions, start),
            mockMethod.getMockName());
        boolean shouldAppendTypeParameter = !mockMethod.getDesc().equals(mockMethod.getMockDesc());
        String testClassName = ClassUtil.getTestClassName(cn.name);
        if (Opcodes.INVOKESTATIC == opcode || isCompanionMethod(ownerClass, opcode)) {
            if (shouldAppendTypeParameter) {
                // append a null value if it was a static invoke or in kotlin companion class
                mn.instructions.insertBefore(instructions[start], new InsnNode(ACONST_NULL));
            }
            if (ClassUtil.isCompanionClassName(ownerClass)) {
                // for kotlin companion class, remove the byte code of reference to "companion" static field
                mn.instructions.remove(instructions[end - 1]);
            }
        } else if (!shouldAppendTypeParameter) {
            // remove extra target ops code
            mn.instructions.remove(instructions[start]);
        }
        // method with @MockMethod will be modified as public static access, so INVOKESTATIC is used
        mn.instructions.insertBefore(instructions[end], new MethodInsnNode(INVOKESTATIC, testClassName,
            mockMethod.getMockName(), mockMethod.getMockDesc(), false));
        mn.instructions.remove(instructions[end]);
        return new ModifiedInsnNodes(mn.instructions.toArray(), 1);
    }

    private boolean isCompanionMethod(String ownerClass, int opcode) {
        return Opcodes.INVOKEVIRTUAL == opcode && ClassUtil.isCompanionClassName(ownerClass);
    }

}
