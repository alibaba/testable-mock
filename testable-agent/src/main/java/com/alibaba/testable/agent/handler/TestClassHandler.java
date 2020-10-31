package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.constant.ConstPool;
import com.alibaba.testable.agent.tool.ImmutablePair;
import com.alibaba.testable.agent.util.AnnotationUtil;
import com.alibaba.testable.agent.util.ClassUtil;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author flin
 */
public class TestClassHandler extends BaseClassHandler {

    private static final String CLASS_TESTABLE_TOOL = "com/alibaba/testable/core/tool/TestableTool";
    private static final String CLASS_TESTABLE_UTIL = "com/alibaba/testable/core/util/TestableUtil";
    private static final String CLASS_INVOKE_RECORD_UTIL = "com/alibaba/testable/core/util/InvokeRecordUtil";
    private static final String FIELD_TEST_CASE = "TEST_CASE";
    private static final String FIELD_SOURCE_METHOD = "SOURCE_METHOD";
    private static final String METHOD_CURRENT_TEST_CASE_NAME = "currentTestCaseName";
    private static final String METHOD_CURRENT_SOURCE_METHOD_NAME = "currentSourceMethodName";
    private static final String METHOD_RECORD_MOCK_INVOKE = "recordMockInvoke";
    private static final String SIGNATURE_TESTABLE_UTIL_METHOD = "(Ljava/lang/Object;)Ljava/lang/String;";
    private static final String SIGNATURE_INVOKE_RECORDER_METHOD = "([Ljava/lang/Object;Z)V";
    private static final Map<String, String> FIELD_TO_METHOD_MAPPING = new HashMap<String, String>() {{
        put(FIELD_TEST_CASE, METHOD_CURRENT_TEST_CASE_NAME);
        put(FIELD_SOURCE_METHOD, METHOD_CURRENT_SOURCE_METHOD_NAME);
    }};

    /**
     * Handle bytecode of test class
     * @param cn original class node
     */
    @Override
    protected void transform(ClassNode cn) {
        for (MethodNode m : cn.methods) {
            transformMethod(cn, m);
        }
        cn.fields.add(new FieldNode(ACC_PUBLIC | ACC_STATIC, ConstPool.TESTABLE_INJECT_REF,
            ClassUtil.toByteCodeClassName(cn.name), null, null));
    }

    private void transformMethod(ClassNode cn, MethodNode mn) {
        handleAnnotation(cn, mn);
        handleInstruction(mn);
    }

    private void handleAnnotation(ClassNode cn, MethodNode mn) {
        List<String> visibleAnnotationNames = new ArrayList<String>();
        if (mn.visibleAnnotations == null) {
            return;
        }
        for (AnnotationNode n : mn.visibleAnnotations) {
            visibleAnnotationNames.add(n.desc);
        }
        if (visibleAnnotationNames.contains(ClassUtil.toByteCodeClassName(ConstPool.TESTABLE_MOCK))) {
            mn.access &= ~ACC_PRIVATE;
            mn.access &= ~ACC_PROTECTED;
            mn.access |= ACC_PUBLIC;
            injectInvokeRecorder(mn);
        } else if (couldBeTestMethod(mn)) {
            injectTestableRef(cn, mn);
        }
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
        il.add(new VarInsnNode(ALOAD, 0));
        il.add(new MethodInsnNode(INVOKESTATIC, CLASS_TESTABLE_UTIL, FIELD_TO_METHOD_MAPPING.get(fieldName),
            SIGNATURE_TESTABLE_UTIL_METHOD, false));
        mn.instructions.insert(instructions[pos], il);
        mn.instructions.remove(instructions[pos]);
        return mn.instructions.toArray();
    }

    private void injectInvokeRecorder(MethodNode mn) {
        InsnList il = new InsnList();
        List<Byte> types = ClassUtil.getParameterTypes(mn.desc);
        int size = mn.parameters.size();
        int parameterOffset = 1;
        il.add(getIntInsn(size));
        il.add(new TypeInsnNode(ANEWARRAY, ClassUtil.CLASS_OBJECT));
        for (int i = 0; i < size; i++) {
            mn.maxStack += 2;
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
            String method = AnnotationUtil.getAnnotationParameter(an, ConstPool.FIELD_TARGET_METHOD, null);
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

    private void injectTestableRef(ClassNode cn, MethodNode mn) {
        InsnList il = new InsnList();
        il.add(new VarInsnNode(ALOAD, 0));
        il.add(new FieldInsnNode(PUTSTATIC, cn.name, ConstPool.TESTABLE_INJECT_REF,
            ClassUtil.toByteCodeClassName(cn.name)));
        mn.instructions.insertBefore(mn.instructions.get(0), il);
    }

    /**
     * Different unit test framework may have different @Test annotation
     * but they should always NOT private, protected or static
     */
    private boolean couldBeTestMethod(MethodNode mn) {
        return (mn.access & (ACC_PRIVATE | ACC_PROTECTED | ACC_STATIC)) == 0 ;
    }

}
