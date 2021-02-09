package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.constant.ConstPool;
import com.alibaba.testable.agent.tool.ImmutablePair;
import com.alibaba.testable.agent.util.AnnotationUtil;
import com.alibaba.testable.agent.util.ClassUtil;
import com.alibaba.testable.core.util.LogUtil;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import javax.lang.model.type.NullType;
import java.util.List;

import static com.alibaba.testable.agent.util.ClassUtil.toDotSeparateFullClassName;

/**
 * @author flin
 */
public class TestClassHandler extends BaseClassHandler {

    private static final String CLASS_TESTABLE_TOOL = "com/alibaba/testable/core/tool/TestableTool";
    private static final String CLASS_TESTABLE_UTIL = "com/alibaba/testable/core/util/TestableUtil";
    private static final String CLASS_INVOKE_RECORD_UTIL = "com/alibaba/testable/core/util/InvokeRecordUtil";
    private static final String CLASS_MOCK_CONTEXT = "com/alibaba/testable/agent/model/MockContext";
    private static final String REF_TESTABLE_CONTEXT = "_testableContextReference";
    private static final String FIELD_TEST_CASE = "TEST_CASE";
    private static final String FIELD_SOURCE_METHOD = "SOURCE_METHOD";
    private static final String FIELD_MOCK_CONTEXT = "MOCK_CONTEXT";
    private static final String FIELD_PARAMETERS = "parameters";
    private static final String METHOD_CURRENT_TEST_CASE_NAME = "currentTestCaseName";
    private static final String METHOD_CURRENT_SOURCE_METHOD_NAME = "currentSourceMethodName";
    private static final String METHOD_RECORD_MOCK_INVOKE = "recordMockInvoke";
    private static final String SIGNATURE_CURRENT_TEST_CASE_NAME = "(Ljava/lang/String;)Ljava/lang/String;";
    private static final String SIGNATURE_CURRENT_SOURCE_METHOD_NAME = "()Ljava/lang/String;";
    private static final String SIGNATURE_INVOKE_RECORDER_METHOD = "([Ljava/lang/Object;ZZ)V";
    private static final String SIGNATURE_PARAMETERS = "Ljava/util/Map;";

    /**
     * Handle bytecode of test class
     * @param cn original class node
     */
    @Override
    protected void transform(ClassNode cn) {
        if (wasTransformed(cn, REF_TESTABLE_CONTEXT, ClassUtil.toByteCodeClassName(CLASS_MOCK_CONTEXT))) {
            return;
        }
        for (MethodNode mn : cn.methods) {
            if (mn.name.equals(ConstPool.CONSTRUCTOR)) {
                initMockContextReference(cn, mn);
            } else {
                handleMockMethod(cn, mn);
                handleInstruction(cn, mn);
            }
        }
    }

    private void initMockContextReference(ClassNode cn, MethodNode mn) {
        InsnList il = new InsnList();
        il.add(new TypeInsnNode(NEW, CLASS_MOCK_CONTEXT));
        il.add(new InsnNode(DUP));
        il.add(new MethodInsnNode(INVOKESPECIAL, CLASS_MOCK_CONTEXT, "<init>", "()V", false));
        il.add(new FieldInsnNode(PUTSTATIC, cn.name, REF_TESTABLE_CONTEXT,
            ClassUtil.toByteCodeClassName(CLASS_MOCK_CONTEXT)));
        mn.instructions.insertBefore(mn.instructions.get(0), il);
        mn.maxStack++;
    }

    private void handleMockMethod(ClassNode cn, MethodNode mn) {
        if (isMockMethod(mn)) {
            toPublicStatic(cn, mn);
            injectInvokeRecorder(mn);
        }
    }

    private void toPublicStatic(ClassNode cn, MethodNode mn) {
        mn.access &= ~ACC_PRIVATE;
        mn.access &= ~ACC_PROTECTED;
        mn.access |= ACC_PUBLIC;
        if ((mn.access & ACC_STATIC) == 0) {
            mn.access |= ACC_STATIC;
            // remove `this` reference
            LocalVariableNode thisRef = null;
            for (LocalVariableNode vn : mn.localVariables) {
                if (vn.index == 0) {
                    thisRef = vn;
                } else {
                    vn.index--;
                }
            }
            if (thisRef != null) {
                mn.localVariables.remove(thisRef);
            } else {
                LogUtil.error("Fail to find `this` reference in non-static method " + getName(cn, mn));
                return;
            }
            for (AbstractInsnNode in : mn.instructions) {
                if (in instanceof IincInsnNode) {
                    ((IincInsnNode)in).var--;
                } else if (in instanceof VarInsnNode) {
                    if (((VarInsnNode)in).var > 0) {
                        ((VarInsnNode)in).var--;
                    } else if (in.getOpcode() == ALOAD) {
                        LogUtil.error("Attempt to access non-static member in mock method " + getName(cn, mn));
                        return;
                    }
                }
            }
            mn.maxLocals--;
        }
    }

    private String getName(ClassNode cn, MethodNode mn) {
        return cn.name + ":" + mn.name;
    }

    private boolean isMockMethod(MethodNode mn) {
        if (mn.visibleAnnotations == null) {
            return false;
        }
        for (AnnotationNode an : mn.visibleAnnotations) {
            if (ClassUtil.toByteCodeClassName(ConstPool.MOCK_METHOD).equals(an.desc) ||
                ClassUtil.toByteCodeClassName(ConstPool.MOCK_CONSTRUCTOR).equals(an.desc)) {
                return true;
            }
        }
        return false;
    }

    private void handleInstruction(ClassNode cn, MethodNode mn) {
        AbstractInsnNode[] instructions = mn.instructions.toArray();
        int i = 0;
        do {
            if (instructions[i].getOpcode() == GETSTATIC) {
                FieldInsnNode fieldInsnNode = (FieldInsnNode)instructions[i];
                if (isTestableUtilField(fieldInsnNode)) {
                    instructions = replaceTestableUtilField(cn, mn, instructions, fieldInsnNode.name, i);
                }
            }
            i++;
        } while (i < instructions.length);
    }

    private boolean isTestableUtilField(FieldInsnNode fieldInsnNode) {
        return fieldInsnNode.owner.equals(CLASS_TESTABLE_TOOL) &&
            (fieldInsnNode.name.equals(FIELD_TEST_CASE) || fieldInsnNode.name.equals(FIELD_SOURCE_METHOD) ||
                fieldInsnNode.name.equals(FIELD_MOCK_CONTEXT));
    }

    private AbstractInsnNode[] replaceTestableUtilField(ClassNode cn, MethodNode mn, AbstractInsnNode[] instructions,
                                                        String fieldName, int pos) {
        InsnList il = new InsnList();
        if (FIELD_TEST_CASE.equals(fieldName)) {
            il.add(new LdcInsnNode(ClassUtil.toDotSeparatedName(cn.name)));
            il.add(new MethodInsnNode(INVOKESTATIC, CLASS_TESTABLE_UTIL, METHOD_CURRENT_TEST_CASE_NAME,
                SIGNATURE_CURRENT_TEST_CASE_NAME, false));
        } else if (FIELD_SOURCE_METHOD.equals(fieldName)) {
            il.add(new MethodInsnNode(INVOKESTATIC, CLASS_TESTABLE_UTIL, METHOD_CURRENT_SOURCE_METHOD_NAME,
                SIGNATURE_CURRENT_SOURCE_METHOD_NAME, false));
        } else if (FIELD_MOCK_CONTEXT.equals(fieldName)) {
            il.add(new FieldInsnNode(GETSTATIC, cn.name, REF_TESTABLE_CONTEXT,
                ClassUtil.toByteCodeClassName(CLASS_MOCK_CONTEXT)));
            il.add(new FieldInsnNode(GETFIELD, CLASS_MOCK_CONTEXT, FIELD_PARAMETERS, SIGNATURE_PARAMETERS));
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
        int parameterOffset = 0;
        mn.maxStack += 2;
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
        if (isTargetClassInParameter(mn)) {
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
            String annotationName = toDotSeparateFullClassName(an.desc);
            if (ConstPool.MOCK_CONSTRUCTOR.equals(annotationName)) {
                return true;
            } else if (ConstPool.MOCK_METHOD.equals(annotationName)) {
                String method = AnnotationUtil.getAnnotationParameter
                    (an, ConstPool.FIELD_TARGET_METHOD, null, String.class);
                if (ConstPool.CONSTRUCTOR.equals(method)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTargetClassInParameter(MethodNode mn) {
        for (AnnotationNode an : mn.visibleAnnotations) {
            if (ConstPool.MOCK_METHOD.equals(toDotSeparateFullClassName(an.desc))) {
                Type type = AnnotationUtil.getAnnotationParameter(an, ConstPool.FIELD_TARGET_CLASS, null, Type.class);
                if (type != null && !type.getClassName().equals(NullType.class.getName())) {
                    return false;
                }
            }
        }
        return true;
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
