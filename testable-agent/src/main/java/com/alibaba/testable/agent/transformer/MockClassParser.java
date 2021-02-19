package com.alibaba.testable.agent.transformer;

import com.alibaba.testable.agent.constant.ConstPool;
import com.alibaba.testable.agent.model.MethodInfo;
import com.alibaba.testable.agent.tool.ImmutablePair;
import com.alibaba.testable.agent.util.AnnotationUtil;
import com.alibaba.testable.agent.util.ClassUtil;
import com.alibaba.testable.agent.util.DiagnoseUtil;
import com.alibaba.testable.agent.util.MethodUtil;
import com.alibaba.testable.core.util.LogUtil;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

import static com.alibaba.testable.agent.util.ClassUtil.toDotSeparateFullClassName;
import static com.alibaba.testable.agent.util.MethodUtil.isStatic;
import static com.alibaba.testable.core.constant.ConstPool.CONSTRUCTOR;

public class MockClassParser {

    private static final String CLASS_OBJECT = "java/lang/Object";

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
            checkMethodAnnotation(cn, methodInfos, mn);
        }
        LogUtil.diagnose("  Found %d mock methods", methodInfos.size());
        return methodInfos;
    }

    /**
     * Check whether any method in specified class has mock-related annotation
     *
     * @param className class that need to explore
     * @return found annotation or not
     */
    public boolean isMockClass(String className) {
        ClassNode cn = ClassUtil.getClassNode(className);
        if (cn == null) {
            return false;
        }
        DiagnoseUtil.setupByClass(cn);
        for (MethodNode mn : cn.methods) {
            if (mn.visibleAnnotations != null) {
                for (AnnotationNode an : mn.visibleAnnotations) {
                    String fullClassName = toDotSeparateFullClassName(an.desc);
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
        return mns;
    }

    private void checkMethodAnnotation(ClassNode cn, List<MethodInfo> methodInfos, MethodNode mn) {
        if (mn.visibleAnnotations == null) {
            return;
        }
        for (AnnotationNode an : mn.visibleAnnotations) {
            String fullClassName = toDotSeparateFullClassName(an.desc);
            if (fullClassName.equals(ConstPool.MOCK_CONSTRUCTOR)) {
                LogUtil.verbose("   Mock constructor \"%s\" as \"(%s)V\" for \"%s\"", mn.name,
                    MethodUtil.extractParameters(mn.desc), MethodUtil.getReturnType(mn.desc));
                addMockConstructor(methodInfos, cn, mn);
            } else if (fullClassName.equals(ConstPool.MOCK_METHOD)) {
                LogUtil.verbose("   Mock method \"%s\" as \"%s\"", mn.name, getTargetMethodDesc(mn, an));
                String targetMethod = AnnotationUtil.getAnnotationParameter(
                    an, ConstPool.FIELD_TARGET_METHOD, mn.name, String.class);
                if (CONSTRUCTOR.equals(targetMethod)) {
                    addMockConstructor(methodInfos, cn, mn);
                } else {
                    MethodInfo mi = getMethodInfo(mn, an, targetMethod);
                    if (mi != null) {
                        methodInfos.add(mi);
                    }
                }
                break;
            }
        }
    }

    private String getTargetMethodDesc(MethodNode mn, AnnotationNode mockMethodAnnotation) {
        Type type = AnnotationUtil.getAnnotationParameter(mockMethodAnnotation, ConstPool.FIELD_TARGET_CLASS,
            null, Type.class);
        return type == null ? MethodUtil.removeFirstParameter(mn.desc) : mn.desc;
    }

    private MethodInfo getMethodInfo(MethodNode mn, AnnotationNode an, String targetMethod) {
        Type targetType = AnnotationUtil.getAnnotationParameter(an, ConstPool.FIELD_TARGET_CLASS, null, Type.class);
        boolean isStatic = isStatic(mn);
        if (targetType == null) {
            // "targetClass" unset, use first parameter as target class type
            ImmutablePair<String, String> methodDescPair = extractFirstParameter(mn.desc);
            if (methodDescPair == null) {
                return null;
            }
            return new MethodInfo(methodDescPair.left, targetMethod, methodDescPair.right, mn.name, mn.desc, isStatic);
        } else {
            // "targetClass" found, use it as target class type
            String slashSeparatedName = ClassUtil.toSlashSeparatedName(targetType.getClassName());
            return new MethodInfo(slashSeparatedName, targetMethod, mn.desc, mn.name,
                MethodUtil.addParameterAtBegin(mn.desc, ClassUtil.toByteCodeClassName(slashSeparatedName)), isStatic);
        }
    }

    private void addMockConstructor(List<MethodInfo> methodInfos, ClassNode cn, MethodNode mn) {
        String sourceClassName = ClassUtil.getSourceClassName(cn.name);
        methodInfos.add(new MethodInfo(sourceClassName, CONSTRUCTOR, mn.desc, mn.name, mn.desc, isStatic(mn)));
    }

    /**
     * Split desc to "first parameter" and "desc of rest parameters"
     * @param desc method desc
     */
    private ImmutablePair<String, String> extractFirstParameter(String desc) {
        // assume first parameter is a class
        int pos = desc.indexOf(";");
        return pos < 0 ? null : ImmutablePair.of(desc.substring(2, pos), "(" + desc.substring(pos + 1));
    }


}
