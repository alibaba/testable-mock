package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.constant.ByteCodeConst;
import com.alibaba.testable.agent.constant.ConstPool;
import com.alibaba.testable.agent.tool.ImmutablePair;
import com.alibaba.testable.agent.util.AnnotationUtil;
import com.alibaba.testable.agent.util.ClassUtil;
import com.alibaba.testable.agent.util.GlobalConfig;
import com.alibaba.testable.agent.util.MethodUtil;
import com.alibaba.testable.core.model.MockScope;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;

import static com.alibaba.testable.agent.constant.ByteCodeConst.TYPE_ARRAY;
import static com.alibaba.testable.agent.constant.ByteCodeConst.TYPE_CLASS;
import static com.alibaba.testable.agent.util.ClassUtil.toDotSeparateFullClassName;
import static com.alibaba.testable.core.constant.ConstPool.CONSTRUCTOR;

/**
 * @author flin
 */
public class MockClassHandler extends BaseClassWithContextHandler {

    private static final String CLASS_INVOKE_RECORD_UTIL = "com/alibaba/testable/core/util/InvokeRecordUtil";
    private static final String CLASS_MOCK_ASSOCIATION_UTIL = "com/alibaba/testable/core/util/MockAssociationUtil";
    private static final String METHOD_INVOKE_ORIGIN = "invokeOrigin";
    private static final String SIGNATURE_INVOKE_ORIGIN =
        "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;";
    private static final String METHOD_RECORD_MOCK_INVOKE = "recordMockInvoke";
    private static final String SIGNATURE_RECORDER_METHOD_INVOKE = "([Ljava/lang/Object;Z)V";
    private static final String METHOD_IS_ASSOCIATED = "isAssociated";
    private static final String SIGNATURE_IS_ASSOCIATED = "()Z";

    public MockClassHandler(String className) {
        this.mockClassName = className;
    }

    @Override
    protected void transform(ClassNode cn) {
        injectGetInstanceMethod(cn);
        for (MethodNode mn : cn.methods) {
            if (isMockMethod(mn)) {
                mn.access &= ~ACC_PRIVATE;
                mn.access &= ~ACC_PROTECTED;
                mn.access |= ACC_PUBLIC;
                // firstly, unfold target class from annotation to parameter
                unfoldTargetClass(mn);
                // secondly, add invoke recorder at the beginning of mock method
                injectInvokeRecorder(mn);
                // thirdly, add association checker before invoke recorder
                injectAssociationChecker(mn);
                // finally, handle testable util variables
                handleTestableUtil(mn);
            }
        }
    }

    /**
     * add method to fetch singleton instance of this mock class
     */
    private void injectGetInstanceMethod(ClassNode cn) {
        MethodNode getInstanceMethod = new MethodNode(ACC_PUBLIC | ACC_STATIC, GET_TESTABLE_REF,
            VOID_ARGS + ClassUtil.toByteCodeClassName(mockClassName), null, null);
        InsnList il = new InsnList();
        il.add(new FieldInsnNode(GETSTATIC, mockClassName, TESTABLE_REF, ClassUtil.toByteCodeClassName(mockClassName)));
        LabelNode label = new LabelNode();
        il.add(new JumpInsnNode(IFNONNULL, label));
        il.add(new TypeInsnNode(NEW, mockClassName));
        il.add(new InsnNode(DUP));
        il.add(new MethodInsnNode(INVOKESPECIAL, mockClassName, CONSTRUCTOR, VOID_ARGS + VOID_RES, false));
        il.add(new FieldInsnNode(PUTSTATIC, mockClassName, TESTABLE_REF, ClassUtil.toByteCodeClassName(mockClassName)));
        il.add(label);
        il.add(new FrameNode(F_SAME, 0, null, 0, null));
        il.add(new FieldInsnNode(GETSTATIC, mockClassName, TESTABLE_REF, ClassUtil.toByteCodeClassName(mockClassName)));
        il.add(new InsnNode(ARETURN));
        getInstanceMethod.instructions = il;
        getInstanceMethod.maxStack = 2;
        getInstanceMethod.maxLocals = 0;
        cn.methods.add(getInstanceMethod);
    }

    /**
     * put targetClass parameter in @MockMethod to first parameter of the mock method
     */
    private void unfoldTargetClass(MethodNode mn) {
        String targetClassName = null;
        for (AnnotationNode an : mn.visibleAnnotations) {
            if (ClassUtil.toByteCodeClassName(ConstPool.MOCK_METHOD).equals(an.desc)) {
                Type type = AnnotationUtil.getAnnotationParameter(an, ConstPool.FIELD_TARGET_CLASS, null, Type.class);
                if (type != null) {
                    targetClassName = ClassUtil.toByteCodeClassName(type.getClassName());
                }
                AnnotationUtil.removeAnnotationParameter(an, ConstPool.FIELD_TARGET_CLASS);
            }
        }
        if (targetClassName != null) {
            // must get label before method description changed
            ImmutablePair<LabelNode, LabelNode> labels = getStartAndEndLabel(mn);
            mn.desc = MethodUtil.addParameterAtBegin(mn.desc, targetClassName);
            int parameterOffset = MethodUtil.isStatic(mn) ? 0 : 1;
            mn.localVariables.add(parameterOffset, new LocalVariableNode("__self", targetClassName, null,
                labels.left, labels.right, parameterOffset));
            for (int i = parameterOffset + 1; i < mn.localVariables.size(); i++) {
                mn.localVariables.get(i).index++;
            }
            for (AbstractInsnNode in : mn.instructions) {
                if (in instanceof IincInsnNode) {
                    ((IincInsnNode)in).var++;
                } else if (in instanceof VarInsnNode && ((VarInsnNode)in).var >= parameterOffset) {
                    ((VarInsnNode)in).var++;
                } else if (in instanceof FrameNode && ((FrameNode)in).type == F_FULL) {
                    ((FrameNode)in).local.add(parameterOffset, targetClassName);
                }
            }
            mn.maxLocals++;
        }
    }

    private ImmutablePair<LabelNode, LabelNode> getStartAndEndLabel(MethodNode mn) {
        if (MethodUtil.isStatic(mn)) {
            LabelNode startLabel = null, endLabel = null;
            for (AbstractInsnNode n = mn.instructions.getFirst(); n != null; n = n.getNext()) {
                if (n instanceof LabelNode) {
                    startLabel = (LabelNode)n;
                    break;
                }
            }
            if (MethodUtil.extractParameters(mn.desc).isEmpty()) {
                // for method without parameter, should manually add a ending label
                endLabel = new LabelNode(new Label());
                mn.instructions.add(endLabel);
            } else {
                // for method with parameters, find the existing ending label
                for (AbstractInsnNode n = mn.instructions.getLast(); n != null; n = n.getPrevious()) {
                    if (n instanceof LabelNode) {
                        endLabel = (LabelNode)n;
                        break;
                    }
                }
            }
            return ImmutablePair.of(startLabel, endLabel);
        } else {
            LocalVariableNode thisRef = mn.localVariables.get(0);
            return ImmutablePair.of(thisRef.start, thisRef.end);
        }
    }

    private void injectAssociationChecker(MethodNode mn) {
        if (isGlobalScope(mn)) {
            return;
        }
        LabelNode firstLine = new LabelNode(new Label());
        InsnList il = new InsnList();
        il.add(new MethodInsnNode(INVOKESTATIC, CLASS_MOCK_ASSOCIATION_UTIL, METHOD_IS_ASSOCIATED,
            SIGNATURE_IS_ASSOCIATED, false));
        il.add(new JumpInsnNode(IFNE, firstLine));
        il.add(invokeOriginalMethod(mn));
        il.add(firstLine);
        il.add( new FrameNode(F_SAME, 0, null, 0, null));
        mn.instructions.insertBefore(mn.instructions.getFirst(), il);
    }

    private InsnList invokeOriginalMethod(MethodNode mn) {
        InsnList il = new InsnList();
        ImmutablePair<Type, String> target = getTargetClassAndMethodName(mn);
        il.add(new LdcInsnNode(target.left));
        il.add(new LdcInsnNode(target.right));
        il.add(duplicateParameters(mn));
        il.add(new MethodInsnNode(INVOKESTATIC, CLASS_MOCK_ASSOCIATION_UTIL, METHOD_INVOKE_ORIGIN,
            SIGNATURE_INVOKE_ORIGIN, false));
        String returnType = MethodUtil.getReturnType(mn.desc);
        if (VOID_RES.equals(returnType)) {
            il.add(new InsnNode(POP));
            il.add(new InsnNode(RETURN));
        } else if (returnType.startsWith(String.valueOf(TYPE_ARRAY)) ||
            returnType.startsWith(String.valueOf(TYPE_CLASS))) {
            il.add(new TypeInsnNode(CHECKCAST, returnType));
            il.add(new InsnNode(ARETURN));
        } else {
            String wrapperClass = ClassUtil.toWrapperClass(returnType.getBytes()[0]);
            il.add(new TypeInsnNode(CHECKCAST, wrapperClass));
            ImmutablePair<String, String> convertMethod = ClassUtil.getWrapperTypeConvertMethod(returnType.getBytes()[0]);
            il.add(new MethodInsnNode(INVOKEVIRTUAL, wrapperClass, convertMethod.left, convertMethod.right, false));
            il.add(new InsnNode(ClassUtil.getReturnOpsCode(returnType)));
        }
        return il;
    }

    private ImmutablePair<Type, String> getTargetClassAndMethodName(MethodNode mn) {
        Type className;
        String methodName = mn.name;
        for (AnnotationNode an : mn.visibleAnnotations) {
            if (isMockMethodAnnotation(an)) {
                String name = AnnotationUtil.getAnnotationParameter(an, ConstPool.FIELD_TARGET_METHOD, null, String.class);
                if (name != null) {
                    methodName = name;
                }
            } else if (isMockConstructorAnnotation(an)) {
                methodName = CONSTRUCTOR;
            }
        }
        if (methodName.equals(CONSTRUCTOR)) {
            className = Type.getType(MethodUtil.getReturnType(mn.desc));
        } else {
            className = Type.getType(MethodUtil.getFirstParameter(mn.desc));
        }
        return ImmutablePair.of(className, methodName);
    }

    private boolean isGlobalScope(MethodNode mn) {
        for (AnnotationNode an : mn.visibleAnnotations) {
            if (isMockMethodAnnotation(an) || isMockConstructorAnnotation(an)) {
                MockScope scope = AnnotationUtil.getAnnotationParameter(an, ConstPool.FIELD_SCOPE,
                    GlobalConfig.getDefaultMockScope(), MockScope.class);
                if (scope.equals(MockScope.GLOBAL)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isMockMethod(MethodNode mn) {
        if (mn.visibleAnnotations == null) {
            return false;
        }
        for (AnnotationNode an : mn.visibleAnnotations) {
            if (isMockMethodAnnotation(an) && AnnotationUtil.isValidMockMethod(mn, an)) {
                return true;
            } else if (isMockConstructorAnnotation(an)) {
                return true;
            }
        }
        return false;
    }

    private boolean isMockConstructorAnnotation(AnnotationNode an) {
        return ClassUtil.toByteCodeClassName(ConstPool.MOCK_CONSTRUCTOR).equals(an.desc);
    }

    private boolean isMockMethodAnnotation(AnnotationNode an) {
        return ClassUtil.toByteCodeClassName(ConstPool.MOCK_METHOD).equals(an.desc);
    }

    private void injectInvokeRecorder(MethodNode mn) {
        InsnList il = new InsnList();
        il.add(duplicateParameters(mn));
        if (isMockForConstructor(mn)) {
            il.add(new InsnNode(ICONST_1));
        } else {
            il.add(new InsnNode(ICONST_0));
        }
        il.add(new MethodInsnNode(INVOKESTATIC, CLASS_INVOKE_RECORD_UTIL, METHOD_RECORD_MOCK_INVOKE,
            SIGNATURE_RECORDER_METHOD_INVOKE, false));
        mn.instructions.insertBefore(mn.instructions.getFirst(), il);
        mn.maxStack += (2 + MethodUtil.getParameterTypes(mn.desc).size() * 3);
    }

    private InsnList duplicateParameters(MethodNode mn) {
        InsnList il = new InsnList();
        List<Byte> types = MethodUtil.getParameterTypes(mn.desc);
        int size = types.size();
        il.add(getIntInsn(size));
        il.add(new TypeInsnNode(ANEWARRAY, ClassUtil.CLASS_OBJECT));
        int parameterOffset = MethodUtil.isStatic(mn) ? 0 : 1;
        for (int i = 0; i < size; i++) {
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
        return il;
    }

    private boolean isMockForConstructor(MethodNode mn) {
        for (AnnotationNode an : mn.visibleAnnotations) {
            String annotationName = toDotSeparateFullClassName(an.desc);
            if (ConstPool.MOCK_CONSTRUCTOR.equals(annotationName)) {
                return true;
            } else if (ConstPool.MOCK_METHOD.equals(annotationName)) {
                String method = AnnotationUtil.getAnnotationParameter
                    (an, ConstPool.FIELD_TARGET_METHOD, null, String.class);
                if (CONSTRUCTOR.equals(method)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static ImmutablePair<Integer, Integer> getLoadParameterByteCode(Byte type) {
        switch (type) {
            case ByteCodeConst.TYPE_BYTE:
            case ByteCodeConst.TYPE_CHAR:
            case ByteCodeConst.TYPE_SHORT:
            case ByteCodeConst.TYPE_INT:
            case ByteCodeConst.TYPE_BOOL:
                return ImmutablePair.of(ILOAD, 1);
            case ByteCodeConst.TYPE_DOUBLE:
                return ImmutablePair.of(DLOAD, 2);
            case ByteCodeConst.TYPE_FLOAT:
                return ImmutablePair.of(FLOAD, 1);
            case ByteCodeConst.TYPE_LONG:
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
