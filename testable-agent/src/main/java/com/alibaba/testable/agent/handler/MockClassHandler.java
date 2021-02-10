package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.constant.ConstPool;
import com.alibaba.testable.agent.tool.ImmutablePair;
import com.alibaba.testable.agent.util.AnnotationUtil;
import com.alibaba.testable.agent.util.ClassUtil;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import javax.lang.model.type.NullType;
import java.util.List;

import static com.alibaba.testable.agent.constant.ConstPool.CONSTRUCTOR;
import static com.alibaba.testable.agent.util.ClassUtil.toDotSeparateFullClassName;

/**
 * @author flin
 */
public class MockClassHandler extends BaseClassHandler {

    private static final String REF_INSTANCE = "_instance";
    private static final String CLASS_INVOKE_RECORD_UTIL = "com/alibaba/testable/core/util/InvokeRecordUtil";
    private static final String METHOD_RECORD_MOCK_INVOKE = "recordMockInvoke";
    private static final String SIGNATURE_INVOKE_RECORDER_METHOD = "([Ljava/lang/Object;ZZ)V";

    public MockClassHandler(String className) {
        this.mockClassName = className;
    }

    @Override
    protected void transform(ClassNode cn) {
        if (wasTransformed(cn, REF_INSTANCE, ClassUtil.toByteCodeClassName(mockClassName))) {
            return;
        }
        addGetInstanceMethod(cn);
        for (MethodNode mn : cn.methods) {
            if (isMockMethod(mn)) {
                mn.access &= ~ACC_PRIVATE;
                mn.access &= ~ACC_PROTECTED;
                mn.access |= ACC_PUBLIC;
                injectInvokeRecorder(mn);
            }
        }
    }

    private void addGetInstanceMethod(ClassNode cn) {
        MethodNode getInstanceMethod = new MethodNode(ACC_PUBLIC | ACC_STATIC, REF_GET_INSTANCE,
            VOID_ARGS + ClassUtil.toByteCodeClassName(mockClassName), null, null);
        InsnList il = new InsnList();
        il.add(new FieldInsnNode(GETSTATIC, mockClassName, REF_INSTANCE,
            ClassUtil.toByteCodeClassName(mockClassName)));
        LabelNode label = new LabelNode();
        il.add(new JumpInsnNode(IFNONNULL, label));
        il.add(new TypeInsnNode(NEW, mockClassName));
        il.add(new InsnNode(DUP));
        il.add(new MethodInsnNode(INVOKESPECIAL, mockClassName, CONSTRUCTOR, VOID_ARGS + VOID_RES, false));
        il.add(new FieldInsnNode(PUTSTATIC, mockClassName, REF_INSTANCE, ClassUtil.toByteCodeClassName(mockClassName)));
        il.add(label);
        il.add(new FrameNode(F_SAME, 0, null, 0, null));
        il.add(new FieldInsnNode(GETSTATIC, mockClassName, REF_INSTANCE, ClassUtil.toByteCodeClassName(mockClassName)));
        il.add(new InsnNode(ARETURN));
        getInstanceMethod.instructions = il;
        getInstanceMethod.maxStack = 2;
        getInstanceMethod.maxLocals = 0;
        cn.methods.add(getInstanceMethod);
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

    private void injectInvokeRecorder(MethodNode mn) {
        InsnList il = new InsnList();
        List<Byte> types = ClassUtil.getParameterTypes(mn.desc);
        int size = types.size();
        int parameterOffset = 1;
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
