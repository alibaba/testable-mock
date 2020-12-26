package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.constant.ConstPool;
import com.alibaba.testable.agent.tool.ImmutablePair;
import com.alibaba.testable.agent.util.AnnotationUtil;
import com.alibaba.testable.agent.util.ClassUtil;
import com.alibaba.testable.core.util.LogUtil;
import org.objectweb.asm.tree.*;

import java.util.Iterator;
import java.util.List;

import static com.alibaba.testable.agent.util.ClassUtil.toDotSeparateFullClassName;

/**
 * @author flin
 */
public class TestClassHandler extends BaseClassHandler {

    private static final String CLASS_TESTABLE_TOOL = "com/alibaba/testable/core/tool/TestableTool";
    private static final String CLASS_TESTABLE_UTIL = "com/alibaba/testable/core/util/TestableUtil";
    private static final String CLASS_INVOKE_RECORD_UTIL = "com/alibaba/testable/core/util/InvokeRecordUtil";
    private static final String TESTABLE_MARK_FIELD = "__testable";
    private static final String FIELD_TEST_CASE = "TEST_CASE";
    private static final String FIELD_SOURCE_METHOD = "SOURCE_METHOD";
    private static final String METHOD_CURRENT_TEST_CASE_NAME = "currentTestCaseName";
    private static final String METHOD_CURRENT_SOURCE_METHOD_NAME = "currentSourceMethodName";
    private static final String METHOD_RECORD_MOCK_INVOKE = "recordMockInvoke";
    private static final String SIGNATURE_CURRENT_TEST_CASE_NAME = "(Ljava/lang/Object;)Ljava/lang/String;";
    private static final String SIGNATURE_CURRENT_SOURCE_METHOD_NAME = "()Ljava/lang/String;";
    private static final String SIGNATURE_INVOKE_RECORDER_METHOD = "([Ljava/lang/Object;Z)V";

    /**
     * Handle bytecode of test class
     * @param cn original class node
     */
    @Override
    protected void transform(ClassNode cn) {
        if (wasTransformed(cn)) {
            return;
        }
        for (MethodNode mn : cn.methods) {
            handleMockMethod(mn);
            handleInstruction(mn);
        }
    }

    private boolean wasTransformed(ClassNode cn) {
        Iterator<FieldNode> iterator = cn.fields.iterator();
        if (iterator.hasNext()) {
            if (TESTABLE_MARK_FIELD.equals(iterator.next().name)) {
                // avoid duplicate injection
                LogUtil.verbose("Duplicate injection found, ignore " + cn.name);
                return true;
            }
        }
        cn.fields.add(new FieldNode(ACC_PRIVATE, TESTABLE_MARK_FIELD, "I", null, null));
        return false;
    }

    private void handleMockMethod(MethodNode mn) {
        if (isMockMethod(mn)) {
            mn.access &= ~ACC_PRIVATE;
            mn.access &= ~ACC_PROTECTED;
            mn.access |= ACC_PUBLIC;
            mn.access |= ACC_STATIC;
            injectInvokeRecorder(mn);
        }
    }

    private boolean isMockMethod(MethodNode mn) {
        if (mn.visibleAnnotations == null) {
            return false;
        }
        for (AnnotationNode an : mn.visibleAnnotations) {
            if (ClassUtil.toByteCodeClassName(ConstPool.MOCK_METHOD).equals(an.desc) ||
                ClassUtil.toByteCodeClassName(ConstPool.TESTABLE_MOCK).equals(an.desc) ||
                ClassUtil.toByteCodeClassName(ConstPool.MOCK_CONSTRUCTOR).equals(an.desc)) {
                return true;
            }
        }
        return false;
    }

    private void handleInstruction(MethodNode mn) {
        AbstractInsnNode[] instructions = mn.instructions.toArray();
        int i = 0;
        do {
            if (instructions[i].getOpcode() == GETSTATIC) {
                FieldInsnNode fieldInsnNode = (FieldInsnNode)instructions[i];
                if (isTestableUtilField(fieldInsnNode)) {
                    instructions = replaceTestableUtilField(mn, instructions, fieldInsnNode.name, i);
                }
            }
            i++;
        } while (i < instructions.length);
    }

    private boolean isTestableUtilField(FieldInsnNode fieldInsnNode) {
        return fieldInsnNode.owner.equals(CLASS_TESTABLE_TOOL) &&
            (fieldInsnNode.name.equals(FIELD_TEST_CASE) || fieldInsnNode.name.equals(FIELD_SOURCE_METHOD));
    }

    private AbstractInsnNode[] replaceTestableUtilField(MethodNode mn, AbstractInsnNode[] instructions,
                                                        String fieldName, int pos) {
        InsnList il = new InsnList();
        if (FIELD_TEST_CASE.equals(fieldName)) {
            il.add(new VarInsnNode(ALOAD, 0));
            il.add(new MethodInsnNode(INVOKESTATIC, CLASS_TESTABLE_UTIL, METHOD_CURRENT_TEST_CASE_NAME,
                SIGNATURE_CURRENT_TEST_CASE_NAME, false));
        } else if (FIELD_SOURCE_METHOD.equals(fieldName)) {
            il.add(new MethodInsnNode(INVOKESTATIC, CLASS_TESTABLE_UTIL, METHOD_CURRENT_SOURCE_METHOD_NAME,
                SIGNATURE_CURRENT_SOURCE_METHOD_NAME, false));
        }
        if (il.size() > 0) {
            mn.instructions.insert(instructions[pos], il);
            mn.instructions.remove(instructions[pos]);
        }
        return mn.instructions.toArray();
    }

    private void injectInvokeRecorder(MethodNode mn) {
        InsnList il = new InsnList();
        List<Byte> types = ClassUtil.getParameterTypes(mn.desc);
        int size = types.size();
        int parameterOffset = 1;
        mn.maxStack += 1;
        il.add(getIntInsn(size));
        il.add(new TypeInsnNode(ANEWARRAY, ClassUtil.CLASS_OBJECT));
        for (int i = 0; i < size; i++) {
            mn.maxStack += 3;
            il.add(new InsnNode(DUP));
            il.add(getIntInsn(i));
            ImmutablePair<Integer, Integer> code = getLoadParameterByteCode(types.get(i));
            il.add(new VarInsnNode(code.left, parameterOffset));
            parameterOffset += code.right;
            MethodInsnNode typeConvertMethodNode = ClassUtil.getPrimaryTypeConvertMethod(types.get(i));
            if (typeConvertMethodNode != null) {
                il.add(typeConvertMethodNode);
            }
            il.add(new InsnNode(AASTORE));
        }
        if (isMockForConstructor(mn)) {
            il.add(new InsnNode(ICONST_1));
        } else {
            il.add(new InsnNode(ICONST_0));
        }
        il.add(new MethodInsnNode(INVOKESTATIC, CLASS_INVOKE_RECORD_UTIL, METHOD_RECORD_MOCK_INVOKE,
            SIGNATURE_INVOKE_RECORDER_METHOD, false));
        mn.instructions.insertBefore(mn.instructions.get(0), il);
    }

    private boolean isMockForConstructor(MethodNode mn) {
        for (AnnotationNode an : mn.visibleAnnotations) {
            if (toDotSeparateFullClassName(an.desc).equals(ConstPool.MOCK_CONSTRUCTOR)) {
                return true;
            }
            String method = AnnotationUtil.getAnnotationParameter
                (an, ConstPool.FIELD_TARGET_METHOD, null, String.class);
            if (ConstPool.CONSTRUCTOR.equals(method)) {
                return true;
            }
        }
        return false;
    }

    private static ImmutablePair<Integer, Integer> getLoadParameterByteCode(Byte type) {
        switch (type) {
            case ClassUtil.TYPE_BYTE:
            case ClassUtil.TYPE_CHAR:
            case ClassUtil.TYPE_SHORT:
            case ClassUtil.TYPE_INT:
            case ClassUtil.TYPE_BOOL:
                return ImmutablePair.of(ILOAD, 1);
            case ClassUtil.TYPE_DOUBLE:
                return ImmutablePair.of(DLOAD, 2);
            case ClassUtil.TYPE_FLOAT:
                return ImmutablePair.of(FLOAD, 1);
            case ClassUtil.TYPE_LONG:
                return ImmutablePair.of(LLOAD, 2);
            default:
                return ImmutablePair.of(ALOAD, 1);
        }
    }

    private AbstractInsnNode getIntInsn(int num) {
        switch (num) {
            case 0:
                return new InsnNode(ICONST_0);
            case 1:
                return new InsnNode(ICONST_1);
            case 2:
                return new InsnNode(ICONST_2);
            case 3:
                return new InsnNode(ICONST_3);
            case 4:
                return new InsnNode(ICONST_4);
            case 5:
                return new InsnNode(ICONST_5);
            default:
                return new IntInsnNode(BIPUSH, num);
        }
    }

}
