package com.alibaba.testable.agent.transformer;

import com.alibaba.testable.agent.constant.ConstPool;
import com.alibaba.testable.agent.model.MethodInfo;
import com.alibaba.testable.agent.tool.ImmutablePair;
import com.alibaba.testable.agent.util.*;
import com.alibaba.testable.core.exception.TargetNotExistException;
import com.alibaba.testable.core.util.LogUtil;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

import static com.alibaba.testable.agent.constant.ByteCodeConst.TYPE_CLASS;
import static com.alibaba.testable.agent.constant.ConstPool.CLASS_OBJECT;
import static com.alibaba.testable.agent.constant.ConstPool.KOTLIN_POSTFIX_COMPANION;
import static com.alibaba.testable.agent.util.ClassUtil.toJavaStyleClassName;
import static com.alibaba.testable.agent.util.MethodUtil.isStatic;
import static com.alibaba.testable.core.constant.ConstPool.CONSTRUCTOR;
import static com.alibaba.testable.core.constant.ConstPool.MOCK_POSTFIX;

public class MockClassParser {

    /**
     * Get information of all mock methods
     * @param className mock class name
     * @return list of mock methods
     */
    public List<MethodInfo> getTestableMockMethods(String className) {
        List<MethodInfo> methodInfos = new ArrayList<MethodInfo>();
        ClassNode cn = ClassUtil.getClassNode(className);
        if (cn == null) {
            return new ArrayList<MethodInfo>();
        }
        for (MethodNode mn : getAllMethods(cn)) {
            addMethodWithAnnotationCheck(methodInfos, cn, mn);
        }
        return methodInfos;
    }

    /**
     * Check whether any method in specified class has mock-related annotation
     *
     * @param cn class that need to explore
     * @return found annotation or not
     */
    public boolean isMockClass(ClassNode cn) {
        if (cn == null) {
            return false;
        }
        DiagnoseUtil.setupByClass(cn);
        for (MethodNode mn : cn.name.endsWith(MOCK_POSTFIX) ? getAllMethods(cn) : cn.methods) {
            if (mn.visibleAnnotations != null) {
                for (AnnotationNode an : mn.visibleAnnotations) {
                    String fullClassName = toJavaStyleClassName(an.desc);
                    if (fullClassName.equals(ConstPool.MOCK_METHOD) ||
                        fullClassName.equals(ConstPool.MOCK_CONSTRUCTOR)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private List<MethodNode> getAllMethods(ClassNode cn) {
        List<MethodNode> mns = new ArrayList<MethodNode>(cn.methods);
        if (cn.superName != null && !cn.superName.equals(CLASS_OBJECT)) {
            ClassNode scn = ClassUtil.getClassNode(cn.superName);
            if (scn != null) {
                mns.addAll(getAllMethods(scn));
            }
        }
        for (InnerClassNode innerClass : cn.innerClasses) {
            if (innerClass.name.equals(cn.name + KOTLIN_POSTFIX_COMPANION)) {
                ClassNode scn = ClassUtil.getClassNode(innerClass.name);
                if (scn != null) {
                    mns.addAll(getAllMethods(scn));
                }
            }
        }
        return mns;
    }

    private void addMethodWithAnnotationCheck(List<MethodInfo> methodInfos, ClassNode cn, MethodNode mn) {
        if (mn.visibleAnnotations == null) {
            return;
        }
        for (AnnotationNode an : mn.visibleAnnotations) {
            String fullClassName = toJavaStyleClassName(an.desc);
            if (fullClassName.equals(ConstPool.MOCK_CONSTRUCTOR)) {
                if (GlobalConfig.checkMockTargetExistence) {
                    checkTargetConstructorExists(cn, mn);
                }
                methodInfos.add(new MethodInfo(ClassUtil.getSourceClassName(cn.name), CONSTRUCTOR, mn.desc, cn.name,
                    mn.name, mn.desc, isStatic(mn)));
            } else if (fullClassName.equals(ConstPool.MOCK_METHOD) && isValidMockMethod(mn, an)) {
                if (GlobalConfig.checkMockTargetExistence) {
                    checkTargetMethodExists(cn, mn, an);
                }
                String targetMethod = AnnotationUtil.getAnnotationParameter(
                    an, ConstPool.FIELD_TARGET_METHOD, mn.name, String.class);
                MethodInfo mi = getMethodInfo(cn, mn, an, targetMethod);
                if (mi != null) {
                    methodInfos.add(mi);
                } else {
                    LogUtil.warn("Failed to parse method %s::%s", cn.name, mn.name);
                }
                break;
            }
        }
    }

    private MethodInfo getMethodInfo(ClassNode cn, MethodNode mn, AnnotationNode an, String targetMethod) {
        Type targetType = AnnotationUtil.getAnnotationParameter(an, ConstPool.FIELD_TARGET_CLASS, null, Type.class);
        boolean isStatic = isStatic(mn);
        if (targetType == null) {
            // "targetClass" unset, use first parameter as target class type
            ImmutablePair<String, String> methodDescPair = MethodUtil.splitFirstAndRestParameters(mn.desc);
            if (methodDescPair.left.isEmpty()) {
                return null;
            }
            return new MethodInfo(methodDescPair.left, targetMethod, methodDescPair.right, cn.name, mn.name, mn.desc,
                isStatic);
        } else {
            // "targetClass" found, use it as target class type
            String slashSeparatedName = ClassUtil.toSlashSeparatedName(targetType.getClassName());
            return new MethodInfo(slashSeparatedName, targetMethod, mn.desc, cn.name, mn.name,
                MethodUtil.addParameterAtBegin(mn.desc, ClassUtil.toByteCodeClassName(slashSeparatedName)), isStatic);
        }
    }

    private void checkTargetMethodExists(ClassNode cn, MethodNode mn, AnnotationNode an) {
        String targetMethodName = AnnotationUtil.getAnnotationParameter(an, ConstPool.FIELD_TARGET_METHOD, null, String.class);
        if (targetMethodName == null) {
            targetMethodName = mn.name;
        }
        String targetClassName;
        String targetMethodDesc;
        Type targetClass = AnnotationUtil.getAnnotationParameter(an, ConstPool.FIELD_TARGET_CLASS, null, Type.class);
        if (targetClass != null) {
            targetClassName = targetClass.getClassName();
            targetMethodDesc = mn.desc;
            checkMethodExists(cn.name, mn.name, targetClassName, targetMethodName, targetMethodDesc);
        } else if (mn.desc.charAt(1) == TYPE_CLASS) {
            ImmutablePair<String, String> parameterPair = MethodUtil.splitFirstAndRestParameters(mn.desc);
            targetClassName = ClassUtil.toDotSeparatedName(parameterPair.left);
            targetMethodDesc = parameterPair.right;
            checkMethodExists(cn.name, mn.name, targetClassName, targetMethodName, targetMethodDesc);
        } else {
            throw new TargetNotExistException("target class not exist", cn.name, mn.name);
        }
    }

    private void checkMethodExists(String mockClassName, String mockMethodName, String targetClassName,
                                   String targetMethodName, String targetMethodDesc) {
        ClassNode targetClassNode = ClassUtil.getClassNode(targetClassName);
        if (targetClassNode == null) {
            throw new TargetNotExistException("target class not found", mockClassName, mockMethodName);
        }
        boolean targetFound = false;
        for (MethodNode targetMethodNode : getAllMethods(targetClassNode)) {
            if (targetMethodNode.name.equals(targetMethodName)) {
                targetFound = true;
                if (targetMethodNode.desc.equals(targetMethodDesc)) {
                    return;
                }
            }
        }
        throw new TargetNotExistException(
            targetFound ? "mock method does not match original method" : "no such method in target class",
            mockClassName, mockMethodName);
    }

    private void checkTargetConstructorExists(ClassNode cn, MethodNode mn) {
        String returnType = MethodUtil.getReturnType(mn.desc);
        if (returnType.charAt(0) != TYPE_CLASS) {
            throw new TargetNotExistException("return type is not a class", cn.name, mn.name);
        }
        ClassNode targetClassNode = ClassUtil.getClassNode(ClassUtil.toJavaStyleClassName(returnType));
        if (targetClassNode == null) {
            throw new TargetNotExistException("target class not found", cn.name, mn.name);
        }
        for (MethodNode targetMethodNode : targetClassNode.methods) {
            if (CONSTRUCTOR.equals(targetMethodNode.name) &&
                MethodUtil.getParameters(targetMethodNode.desc).equals(MethodUtil.getParameters(mn.desc))) {
                return;
            }
        }
        throw new TargetNotExistException("no such constructor in target class", cn.name, mn.name);
    }

    /**
     * Check is MockMethod annotation is used on a valid mock method
     * @param mn mock method
     * @param an MockMethod annotation
     * @return valid or not
     */
    private boolean isValidMockMethod(MethodNode mn, AnnotationNode an) {
        Type targetClass = AnnotationUtil.getAnnotationParameter(an, ConstPool.FIELD_TARGET_CLASS, null, Type.class);
        if (targetClass != null) {
            return true;
        }
        String firstParameter = MethodUtil.getFirstParameter(mn.desc);
        return !firstParameter.isEmpty() && firstParameter.charAt(0) == TYPE_CLASS;
    }

}
