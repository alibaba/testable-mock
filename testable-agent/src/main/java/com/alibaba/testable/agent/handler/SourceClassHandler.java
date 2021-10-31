package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.model.BasicType;
import com.alibaba.testable.agent.model.MethodInfo;
import com.alibaba.testable.agent.model.TravelStatus;
import com.alibaba.testable.agent.util.BytecodeUtil;
import com.alibaba.testable.agent.util.ClassUtil;
import com.alibaba.testable.agent.util.MethodUtil;
import com.alibaba.testable.core.util.LogUtil;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.alibaba.testable.core.constant.ConstPool.CONSTRUCTOR;

/**
 * @author flin
 */
public class SourceClassHandler extends BaseClassHandler {

    private final AtomicInteger atomicInteger = new AtomicInteger();
    private final String mockClassName;
    private final List<MethodInfo> injectMethods;
    private final Set<Integer> invokeOps = new HashSet<Integer>() {{
        add(Opcodes.INVOKEVIRTUAL);
        add(Opcodes.INVOKESPECIAL);
        add(Opcodes.INVOKESTATIC);
        add(Opcodes.INVOKEINTERFACE);
    }};

    public SourceClassHandler(List<MethodInfo> injectMethods, String mockClassName) {
        this.injectMethods = injectMethods;
        this.mockClassName = mockClassName;
    }

    /**
     * Handle bytecode of source class
     *
     * @param cn original class node
     */
    @Override
    protected void transform(ClassNode cn) {
        LogUtil.diagnose("Found source class %s", cn.name);
        Set<MethodInfo> memberInjectMethods = new HashSet<MethodInfo>();
        Set<MethodInfo> newOperatorInjectMethods = new HashSet<MethodInfo>();
        for (MethodInfo im : injectMethods) {
            if (im.getName().equals(CONSTRUCTOR)) {
                newOperatorInjectMethods.add(im);
            } else {
                memberInjectMethods.add(im);
            }
        }

        resolveMethodReference(cn);

        for (MethodNode m : cn.methods) {
            transformMethod(m, memberInjectMethods, newOperatorInjectMethods, cn);
        }
    }

    private void transformMethod(MethodNode mn, Set<MethodInfo> memberInjectMethods,
                                 Set<MethodInfo> newOperatorInjectMethods, ClassNode cn) {
        LogUtil.verbose("   Found method %s", mn.name);
        if (mn.name.startsWith("$")) {
            // skip methods e.g. "$jacocoInit"
            return;
        }
        AbstractInsnNode[] instructions = mn.instructions.toArray();
        if (instructions.length == 0) {
            // native method (issue-52)
            return;
        }
        int i = 0;
        do {
            if (invokeOps.contains(instructions[i].getOpcode())) {
                MethodInsnNode node = (MethodInsnNode) instructions[i];
                if (CONSTRUCTOR.equals(node.name)) {
                    if (LogUtil.isVerboseEnabled()) {
                        LogUtil.verbose("     Line %d, constructing \"%s\"", getLineNum(instructions, i),
                                MethodUtil.toJavaMethodDesc(node.owner, node.desc));
                    }
                    MethodInfo newOperatorInjectMethod = getNewOperatorInjectMethod(newOperatorInjectMethods, node);
                    if (newOperatorInjectMethod != null) {
                        // it's a new operation and an inject method for it exist
                        int rangeStart = getConstructorStart(instructions, node.owner, i);
                        if (rangeStart >= 0) {
                            if (rangeStart < i) {
                                handleFrameStackChange(mn, newOperatorInjectMethod, rangeStart, i);
                            }
                            instructions = replaceNewOps(mn, newOperatorInjectMethod, instructions, rangeStart, i);
                            i = rangeStart;
                        }
                    }
                } else {
                    if (LogUtil.isVerboseEnabled()) {
                        LogUtil.verbose("     Line %d, invoking \"%s\"", getLineNum(instructions, i),
                                MethodUtil.toJavaMethodDesc(node.owner, node.name, node.desc));
                    }
                    MethodInfo mockMethod = getMemberInjectMethodName(memberInjectMethods, node);
                    if (mockMethod != null) {
                        // it's a member or static method and an inject method for it exist
                        int rangeStart = getMemberMethodStart(instructions, i);
                        if (rangeStart >= 0) {
                            if (rangeStart < i) {
                                handleFrameStackChange(mn, mockMethod, rangeStart, i);
                            }
                            instructions = replaceMemberCallOps(mn, mockMethod,
                                    instructions, node.owner, node.getOpcode(), rangeStart, i);
                            i = rangeStart;
                        } else {
                            LogUtil.warn("Potential missed mocking at %s:%s", mn.name, getLineNum(instructions, i));
                        }
                    }
                }
            }

            i++;
        } while (i < instructions.length);
    }

    /**
     * find the mock method fit for specified method node
     *
     * @param memberInjectMethods mock methods available
     * @param node                method node to match for
     * @return mock method info
     */
    private MethodInfo getMemberInjectMethodName(Set<MethodInfo> memberInjectMethods, MethodInsnNode node) {
        for (MethodInfo m : memberInjectMethods) {
            String nodeOwner = ClassUtil.fitCompanionClassName(node.owner);
            String nodeName = ClassUtil.fitKotlinAccessorName(node.name);
            // Kotlin accessor method will append a extra type parameter
            String nodeDesc = nodeName.equals(node.name) ? node.desc : MethodUtil.removeFirstParameter(node.desc);
            if (m.getClazz().equals(nodeOwner) && m.getName().equals(nodeName) && m.getDesc().equals(nodeDesc)) {
                return m;
            }
        }
        return null;
    }

    private MethodInfo getNewOperatorInjectMethod(Set<MethodInfo> newOperatorInjectMethods, MethodInsnNode node) {
        for (MethodInfo m : newOperatorInjectMethods) {
            if (m.getDesc().equals(getConstructorInjectDesc(node))) {
                return m;
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
            if (instructions[i].getOpcode() == Opcodes.NEW && ((TypeInsnNode) instructions[i]).desc.equals(target)) {
                return i;
            }
        }
        return -1;
    }

    private int getMemberMethodStart(AbstractInsnNode[] instructions, int rangeEnd) {
        int stackLevel = getInitialStackLevel((MethodInsnNode) instructions[rangeEnd]);
        if (stackLevel < 0) {
            return rangeEnd;
        }
        Label labelToJump = null;
        TravelStatus status = TravelStatus.Normal;
        for (int i = rangeEnd - 1; i >= 0; i--) {
            switch (status) {
                case Normal:
                    if (instructions[i] instanceof FrameNode) {
                        status = TravelStatus.LookingForLabel;
                    } else {
                        stackLevel += getStackLevelChange(instructions[i]);
                        if (stackLevel < 0) {
                            return i;
                        }
                    }
                    break;
                case LookingForLabel:
                    if (instructions[i] instanceof LabelNode) {
                        labelToJump = ((LabelNode) instructions[i]).getLabel();
                        status = TravelStatus.LookingForJump;
                    }
                    break;
                case LookingForJump:
                    if (instructions[i] instanceof JumpInsnNode &&
                            ((JumpInsnNode) instructions[i]).label.getLabel().equals(labelToJump)) {
                        stackLevel += getStackLevelChange(instructions[i]);
                        labelToJump = null;
                        status = TravelStatus.Normal;
                    }
                    break;
                default:
                    break;
            }
        }
        return -1;
    }

    private int getInitialStackLevel(MethodInsnNode instruction) {
        int stackLevel = MethodUtil.getParameterTypes((instruction).desc).size();
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
                return stackEffectOfInvocation(((MethodInsnNode) instruction).desc) + 1;
            case Opcodes.INVOKESTATIC:
                return stackEffectOfInvocation(((MethodInsnNode) instruction).desc);
            case Opcodes.INVOKEDYNAMIC:
                return stackEffectOfInvocation(((InvokeDynamicInsnNode) instruction).desc);
            case -1:
                // either LabelNode or LineNumberNode
                return 0;
            default:
                return -BytecodeUtil.stackEffect(instruction.getOpcode());
        }
    }

    private int stackEffectOfInvocation(String desc) {
        return MethodUtil.getParameterTypes(desc).size() - (MethodUtil.getReturnType(desc).equals(VOID_RES) ? 0 : 1);
    }

    private AbstractInsnNode[] replaceNewOps(MethodNode mn, MethodInfo newOperatorInjectMethod,
                                             AbstractInsnNode[] instructions, int start, int end) {
        String mockMethodName = newOperatorInjectMethod.getMockName();
        int invokeOpcode = newOperatorInjectMethod.isStatic() ? INVOKESTATIC : INVOKEVIRTUAL;
        String log = String.format("Line %d, mock method \"%s\" used", getLineNum(instructions, start), mockMethodName);
        if (LogUtil.isVerboseEnabled()) {
            LogUtil.verbose(5, log);
        } else {
            LogUtil.diagnose(2, log);
        }
        String classType = ((TypeInsnNode) instructions[start]).desc;
        String constructorDesc = ((MethodInsnNode) instructions[end]).desc;
        if (!newOperatorInjectMethod.isStatic()) {
            mn.instructions.insertBefore(instructions[start], new MethodInsnNode(INVOKESTATIC, mockClassName,
                    GET_TESTABLE_REF, VOID_ARGS + ClassUtil.toByteCodeClassName(mockClassName), false));
        }
        mn.instructions.insertBefore(instructions[end], new MethodInsnNode(invokeOpcode, mockClassName,
                mockMethodName, getConstructorInjectDesc(constructorDesc, classType), false));
        mn.instructions.remove(instructions[start]);
        mn.instructions.remove(instructions[start + 1]);
        mn.instructions.remove(instructions[end]);
        return mn.instructions.toArray();
    }

    private int getLineNum(AbstractInsnNode[] instructions, int start) {
        for (int i = start - 1; i >= 0; i--) {
            if (instructions[i] instanceof LineNumberNode) {
                return ((LineNumberNode) instructions[i]).line;
            }
        }
        return 0;
    }

    private String getConstructorInjectDesc(String constructorDesc, String classType) {
        return constructorDesc.substring(0, constructorDesc.length() - 1) +
                ClassUtil.toByteCodeClassName(classType);
    }

    private AbstractInsnNode[] replaceMemberCallOps(MethodNode mn, MethodInfo mockMethod, AbstractInsnNode[] instructions,
                                                    String ownerClass, int opcode, int start, int end) {
        String log = String.format("Line %d, mock method \"%s\" used", getLineNum(instructions, start),
                mockMethod.getMockName());
        if (LogUtil.isVerboseEnabled()) {
            LogUtil.verbose(5, log);
        } else {
            LogUtil.diagnose(2, log);
        }
        if (!mockMethod.isStatic()) {
            mn.instructions.insertBefore(instructions[start], new MethodInsnNode(INVOKESTATIC, mockClassName,
                    GET_TESTABLE_REF, VOID_ARGS + ClassUtil.toByteCodeClassName(mockClassName), false));
        }
        if (Opcodes.INVOKESTATIC == opcode || isCompanionMethod(ownerClass, opcode)) {
            // append a null value if it was a static invoke or in kotlin companion class
            mn.instructions.insertBefore(instructions[start], new InsnNode(ACONST_NULL));
            mn.maxStack++;
            if (ClassUtil.isCompanionClassName(ownerClass)) {
                // for kotlin companion class, remove the byte code of reference to "companion" static field
                mn.instructions.remove(instructions[end - 1]);
            }
        }
        // method with @MockMethod will be modified as public access
        int invokeOpcode = mockMethod.isStatic() ? INVOKESTATIC : INVOKEVIRTUAL;
        mn.instructions.insertBefore(instructions[end], new MethodInsnNode(invokeOpcode, mockClassName,
                mockMethod.getMockName(), mockMethod.getMockDesc(), false));
        mn.instructions.remove(instructions[end]);
        mn.maxStack++;
        return mn.instructions.toArray();
    }

    private void handleFrameStackChange(MethodNode mn, MethodInfo mockMethod, int start, int end) {
        AbstractInsnNode curInsn = mn.instructions.get(start);
        AbstractInsnNode endInsn = mn.instructions.get(end);
        do {
            if (curInsn instanceof FrameNode) {
                FrameNode fn = (FrameNode) curInsn;
                if (fn.type == F_FULL) {
                    fn.stack.add(0, mockMethod.getMockClass());
                    // remove label reference in stack of frame node
                    for (int i = fn.stack.size() - 1; i >= 0; i--) {
                        if (fn.stack.get(i) instanceof LabelNode) {
                            fn.stack.remove(i);
                        }
                    }
                }
            }
            curInsn = curInsn.getNext();
        } while (!curInsn.equals(endInsn));
    }

    private boolean isCompanionMethod(String ownerClass, int opcode) {
        return Opcodes.INVOKEVIRTUAL == opcode && ClassUtil.isCompanionClassName(ownerClass);
    }

    private void setFinalValue(Field ownerField, Object obj, Object value) throws Exception {
        ownerField.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(ownerField, ownerField.getModifiers() & ~Modifier.FINAL);
        ownerField.set(obj, value);
    }

    private List<Handle> fetchInvokeDynamicHandle(MethodNode mn) {
        List<Handle> handleList = new ArrayList<Handle>();
        for (AbstractInsnNode instruction : mn.instructions) {
            if (instruction.getOpcode() == Opcodes.INVOKEDYNAMIC) {
                InvokeDynamicInsnNode invokeDynamicInsnNode = (InvokeDynamicInsnNode) instruction;
                handleList.add((Handle) invokeDynamicInsnNode.bsmArgs[1]);
            }
        }
        return handleList;
    }

    private void resolveMethodReference(ClassNode cn) {
        List<Handle> invokeDynamicList = new ArrayList<Handle>();
        for (MethodNode method : cn.methods) {
            List<Handle> handleList = fetchInvokeDynamicHandle(method);
            invokeDynamicList.addAll(handleList);
        }

        // process for method reference
        for (Handle handle : invokeDynamicList) {
            // the jdk auto generation method
            if (handle.getName().startsWith("lambda$")) {
                continue;
            }

            int tag = handle.getTag();

            if (tag == Opcodes.H_NEWINVOKESPECIAL) {
                // lambda new method reference
                continue;
            }

            // external mean:
            // public void foo() {
            //        String s = "";
            //        consumes(s::contains);
            //}
            boolean external = tag == Opcodes.H_INVOKEVIRTUAL;

            boolean isStatic = tag == Opcodes.H_INVOKESTATIC || external;

            String desc = handle.getDesc();
            String parameters = desc.substring(desc.indexOf("(") + 1, desc.lastIndexOf(")"));
            String returnType = desc.substring(desc.indexOf(")") + 1);
            String[] parameterArray = parameters.split(";");
            int len = parameterArray.length;
            for (String s : parameterArray) {
                if (s.isEmpty()) {
                    len--;
                }
            }

            len = external ? len + 1 : len;

            String[] refineParameterArray = new String[len];
            int index = external ? 1 : 0;
            if (external) {
                // The type should was reference type
                refineParameterArray[0] = "L" + handle.getOwner();
            }
            for (String s : parameterArray) {
                if (!s.isEmpty()) {
                    refineParameterArray[index] = s;
                    index++;
                }
            }

            String externalDesc = buildDesc(refineParameterArray, returnType);

            String lambdaName = String.format("Lambda$_%s_%d", handle.getName(), atomicInteger.incrementAndGet());
            MethodVisitor mv = cn.visitMethod(isStatic ? ACC_PUBLIC + ACC_STATIC : ACC_PUBLIC, lambdaName, external ? externalDesc : desc, null, null);
            mv.visitCode();

            Label l0 = new Label();
            mv.visitLabel(l0);
            if (!isStatic) {
                // add this
                mv.visitVarInsn(ALOAD, 0);
            }
            for (int i = 0; i < refineParameterArray.length; i++) {
                String arg = refineParameterArray[i];
                mv.visitVarInsn(getLoadType(arg), isStatic ? i : i + 1);
            }

            mv.visitMethodInsn(isStatic ? INVOKESTATIC : INVOKEVIRTUAL, handle.getOwner(), handle.getName(), desc, false);

            mv.visitInsn(getReturnType(returnType));

            Label l1 = new Label();
            mv.visitLabel(l1);

            // static function was not required add this to first parameter
            if (isStatic) {
                for (int i = 0; i < refineParameterArray.length; i++) {
                    String localVar = refineParameterArray[i];
                    if (!isPrimitive(localVar)) {
                        // primitive type and reference type difference
                        localVar = localVar.endsWith(";") ? localVar : localVar + ";";
                    }

                    if (localVar.isEmpty()) {
                        continue;
                    }

                    // add local var
                    mv.visitLocalVariable(String.format("o%d", i), localVar, null, l0, l1, i);
                }
            } else {
                mv.visitLocalVariable("this", "L" + handle.getOwner() + ";", null, l0, l1, 0);
                for (int i = 0; i < refineParameterArray.length; i++) {
                    String localVar = refineParameterArray[i];
                    if (!isPrimitive(localVar) && !isPrimitiveArray(localVar)) {
                        localVar = localVar.endsWith(";") ? localVar : localVar + ";";
                    }
                    if (localVar.isEmpty()) {
                        continue;
                    }

                    mv.visitLocalVariable(String.format("o%d", i), localVar, null, l0, l1, i + 1);
                }
            }
            // auto compute max
            mv.visitMaxs(-1, -1);
            mv.visitEnd();

            try {
                // modify handle to the generation method
                setFinalValue(handle.getClass().getDeclaredField("name"), handle, lambdaName);
                // mark: should merge the below two if.
                if (!handle.getOwner().equals(cn.name) && isStatic) {
                    setFinalValue(handle.getClass().getDeclaredField("owner"), handle, cn.name);
                }
                if (external) {
                    setFinalValue(handle.getClass().getDeclaredField("owner"), handle, cn.name);
                    setFinalValue(handle.getClass().getDeclaredField("descriptor"), handle, externalDesc);
                    setFinalValue(handle.getClass().getDeclaredField("tag"), handle, H_INVOKESTATIC);
                }
            } catch (Exception ignore) {
            }
        }
    }

    private String buildDesc(String[] refineParameterArray, String returnType) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (String s : refineParameterArray) {
            sb.append(s);
            if (!isPrimitive(s)) {
                sb.append(";");
            }
        }
        sb.append(")");
        sb.append(returnType);
        return sb.toString();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isPrimitive(String type) {
        if (type.endsWith(";")) {
            type = type.substring(0, type.length() - 1);
        }
        return BasicType.basicType(type.charAt(0)).isPrimitive();
    }

    private boolean isPrimitiveArray(String type) {
        if (!type.startsWith("[")) {
            return false;
        }
        if (type.endsWith(";")) {
            type = type.substring(0, type.length() - 1);
        }

        type = type.replace("[", "");
        return BasicType.basicType(type.charAt(0)).isPrimitive();
    }

    private int getReturnType(String returnType) {
        return BasicType.basicType(returnType.charAt(0)).returnInsn;
    }

    private int getLoadType(String arg) {
        return BasicType.basicType(arg.charAt(0)).loadVarInsn;
    }

}
