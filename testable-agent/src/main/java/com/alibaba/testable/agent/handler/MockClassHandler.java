package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.constant.ConstPool;
import com.alibaba.testable.agent.tool.ImmutablePair;
import com.alibaba.testable.agent.util.*;
import com.alibaba.testable.core.model.MockScope;
import com.alibaba.testable.core.util.LogUtil;
import com.alibaba.testable.core.util.MockAssociationUtil;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;

import static com.alibaba.testable.agent.constant.ByteCodeConst.TYPE_ARRAY;
import static com.alibaba.testable.agent.constant.ByteCodeConst.TYPE_CLASS;
import static com.alibaba.testable.agent.constant.ConstPool.CLASS_OBJECT;
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
    private static final String SELF_REF = "__self";
    private static final String TESTABLE_REF = "__testable";

    private final String mockClassName;

    public MockClassHandler(String className) {
        this.mockClassName = className;
    }

    @Override
    protected void transform(ClassNode cn) {
        LogUtil.diagnose("Found mock class %s", cn.name);
        if (!CLASS_OBJECT.equals(cn.superName)) {
            MockAssociationUtil.recordSubMockContainer(ClassUtil.toDotSeparatedName(cn.superName),
                ClassUtil.toDotSeparatedName(cn.name));
        }
        injectRefFieldAndGetInstanceMethod(cn);
        int mockMethodCount = 0;
        for (MethodNode mn : cn.methods) {
            if (isMockMethod(mn)) {
                mockMethodCount++;
                mn.access = BytecodeUtil.toPublicAccess(mn.access);
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
        LogUtil.diagnose("  Found %d mock methods", mockMethodCount);
    }

    /**
     * add method to fetch singleton instance of this mock class
     */
    private void injectRefFieldAndGetInstanceMethod(ClassNode cn) {
        String byteCodeMockClassName = ClassUtil.toByteCodeClassName(mockClassName);
        MethodNode getInstanceMethod = new MethodNode(ACC_PUBLIC | ACC_STATIC, GET_TESTABLE_REF,
            VOID_ARGS + byteCodeMockClassName, null, null);
        InsnList il = new InsnList();
        il.add(new FieldInsnNode(GETSTATIC, mockClassName, TESTABLE_REF, byteCodeMockClassName));
        LabelNode label = new LabelNode();
        il.add(new JumpInsnNode(IFNONNULL, label));
        il.add(new TypeInsnNode(NEW, mockClassName));
        il.add(new InsnNode(DUP));
        il.add(new MethodInsnNode(INVOKESPECIAL, mockClassName, CONSTRUCTOR, VOID_ARGS + VOID_RES, false));
        il.add(new FieldInsnNode(PUTSTATIC, mockClassName, TESTABLE_REF, byteCodeMockClassName));
        il.add(label);
        il.add(new FrameNode(F_SAME, 0, null, 0, null));
        il.add(new FieldInsnNode(GETSTATIC, mockClassName, TESTABLE_REF, byteCodeMockClassName));
        il.add(new InsnNode(ARETURN));
        getInstanceMethod.instructions = il;
        getInstanceMethod.maxStack = 2;
        getInstanceMethod.maxLocals = 0;
        cn.methods.add(getInstanceMethod);
        cn.fields.add(new FieldNode(ACC_PRIVATE | ACC_STATIC, TESTABLE_REF, byteCodeMockClassName, null, null));
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
            // in certain case, local variable table of non-static method can be empty (issue-136)
            int parameterOffset = MethodUtil.isStatic(mn) ? 0 : Math.min(mn.localVariables.size(), 1);
            mn.localVariables.add(parameterOffset, new LocalVariableNode(SELF_REF, targetClassName, null,
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
                    // For groovy adaptation (issue-121)
                    int pos = ((FrameNode)in).local.size() == 0 ? 0 : parameterOffset;
                    ((FrameNode)in).local.add(pos, ClassUtil.toSlashSeparateJavaStyleName(targetClassName));
                }
            }
            mn.maxLocals++;
        }
    }

    private ImmutablePair<LabelNode, LabelNode> getStartAndEndLabel(MethodNode mn) {
        // in certain case, local variable table of non-static method can be empty (issue-136)
        if (MethodUtil.isStatic(mn) || mn.localVariables.isEmpty()) {
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
        il.add(new FrameNode(F_SAME, 0, null, 0, null));
        mn.maxStack = Math.max(6, mn.maxStack);
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
        } else if (returnType.charAt(0) == TYPE_ARRAY || returnType.charAt(0) == TYPE_CLASS) {
            il.add(new TypeInsnNode(CHECKCAST, ClassUtil.toSlashSeparateJavaStyleName(returnType)));
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
                    GlobalConfig.defaultMockScope, MockScope.class);
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
            if (isMockMethodAnnotation(an)) {
                if (LogUtil.isVerboseEnabled()) {
                    LogUtil.verbose("   Mock method \"%s\" as \"%s\"", mn.name, MethodUtil.toJavaMethodDesc(
                        getTargetMethodOwner(mn, an), getTargetMethodName(mn, an), getTargetMethodDesc(mn, an)));
                }
                return true;
            } else if (isMockConstructorAnnotation(an)) {
                if (LogUtil.isVerboseEnabled()) {
                    LogUtil.verbose("   Mock constructor \"%s\" as \"%s\"", mn.name, MethodUtil.toJavaMethodDesc(
                        ClassUtil.toJavaStyleClassName(MethodUtil.getReturnType(mn.desc)), mn.desc));
                }
                return true;
            }
        }
        return false;
    }

    private String getTargetMethodOwner(MethodNode mn, AnnotationNode mockMethodAnnotation) {
        Type type = AnnotationUtil.getAnnotationParameter(mockMethodAnnotation, ConstPool.FIELD_TARGET_CLASS,
            null, Type.class);
        return type == null ? MethodUtil.getFirstParameter(mn.desc) : type.getClassName();
    }

    private String getTargetMethodName(MethodNode mn, AnnotationNode mockMethodAnnotation) {
        String name = AnnotationUtil.getAnnotationParameter(mockMethodAnnotation, ConstPool.FIELD_TARGET_METHOD,
            null, String.class);
        return name == null ? mn.name : name;
    }

    private String getTargetMethodDesc(MethodNode mn, AnnotationNode mockMethodAnnotation) {
        Type type = AnnotationUtil.getAnnotationParameter(mockMethodAnnotation, ConstPool.FIELD_TARGET_CLASS,
            null, Type.class);
        return type == null ? MethodUtil.removeFirstParameter(mn.desc) : mn.desc;
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
        il.add(BytecodeUtil.getIntInsn(size));
        il.add(new TypeInsnNode(ANEWARRAY, CLASS_OBJECT));
        int parameterOffset = MethodUtil.isStatic(mn) ? 0 : 1;
        for (int i = 0; i < size; i++) {
            il.add(new InsnNode(DUP));
            il.add(BytecodeUtil.getIntInsn(i));
            ImmutablePair<Integer, Integer> code = BytecodeUtil.getLoadParameterByteCode(types.get(i));
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
            String annotationName = ClassUtil.toJavaStyleClassName(an.desc);
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

}
