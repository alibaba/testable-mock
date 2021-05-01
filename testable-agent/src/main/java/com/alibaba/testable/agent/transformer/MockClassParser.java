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

import static com.alibaba.testable.agent.constant.ConstPool.CLASS_OBJECT;
import static com.alibaba.testable.agent.util.ClassUtil.toJavaStyleClassName;
import static com.alibaba.testable.agent.util.MethodUtil.isStatic;
import static com.alibaba.testable.core.constant.ConstPool.CONSTRUCTOR;

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
            checkMethodAnnotation(cn, methodInfos, mn);
        }
        LogUtil.diagnose("  Found %d mock methods", methodInfos.size());
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
        for (MethodNode mn : cn.methods) {
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
        return mns;
    }

    private void checkMethodAnnotation(ClassNode cn, List<MethodInfo> methodInfos, MethodNode mn) {
        if (mn.visibleAnnotations == null) {
            return;
        }
        for (AnnotationNode an : mn.visibleAnnotations) {
            String fullClassName = toJavaStyleClassName(an.desc);
            if (fullClassName.equals(ConstPool.MOCK_CONSTRUCTOR)) {
                if (LogUtil.isVerboseEnabled()) {
                    LogUtil.verbose("   Mock constructor \"%s\" as \"%s\"", mn.name, MethodUtil.toJavaMethodDesc(
                        ClassUtil.toJavaStyleClassName(MethodUtil.getReturnType(mn.desc)), mn.desc));
                }
                addMockConstructor(methodInfos, cn, mn);
            } else if (fullClassName.equals(ConstPool.MOCK_METHOD) && AnnotationUtil.isValidMockMethod(mn, an)) {
                if (LogUtil.isVerboseEnabled()) {
                    LogUtil.verbose("   Mock method \"%s\" as \"%s\"", mn.name, MethodUtil.toJavaMethodDesc(
                        getTargetMethodOwner(mn, an), getTargetMethodName(mn, an), getTargetMethodDesc(mn, an)));
                }
                String targetMethod = AnnotationUtil.getAnnotationParameter(
                    an, ConstPool.FIELD_TARGET_METHOD, mn.name, String.class);
                if (CONSTRUCTOR.equals(targetMethod)) {
                    addMockConstructor(methodInfos, cn, mn);
                } else {
                    MethodInfo mi = getMethodInfo(cn, mn, an, targetMethod);
                    if (mi != null) {
                        methodInfos.add(mi);
                    }
                }
                break;
            }
        }
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

    private MethodInfo getMethodInfo(ClassNode cn, MethodNode mn, AnnotationNode an, String targetMethod) {
        Type targetType = AnnotationUtil.getAnnotationParameter(an, ConstPool.FIELD_TARGET_CLASS, null, Type.class);
        boolean isStatic = isStatic(mn);
        if (targetType == null) {
            // "targetClass" unset, use first parameter as target class type
            ImmutablePair<String, String> methodDescPair = extractFirstParameter(mn.desc);
            if (methodDescPair == null) {
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

    private void addMockConstructor(List<MethodInfo> methodInfos, ClassNode cn, MethodNode mn) {
        String sourceClassName = ClassUtil.getSourceClassName(cn.name);
        methodInfos.add(new MethodInfo(sourceClassName, CONSTRUCTOR, mn.desc, cn.name, mn.name, mn.desc, isStatic(mn)));
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
