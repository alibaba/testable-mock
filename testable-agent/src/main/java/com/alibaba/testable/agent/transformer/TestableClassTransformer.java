package com.alibaba.testable.agent.transformer;

import com.alibaba.testable.agent.constant.ConstPool;
import com.alibaba.testable.agent.handler.SourceClassHandler;
import com.alibaba.testable.agent.handler.TestClassHandler;
import com.alibaba.testable.agent.tool.ImmutablePair;
import com.alibaba.testable.agent.model.MethodInfo;
import com.alibaba.testable.agent.tool.ComparableWeakRef;
import com.alibaba.testable.agent.util.AnnotationUtil;
import com.alibaba.testable.agent.util.ClassUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.alibaba.testable.agent.util.ClassUtil.toDotSeparateFullClassName;

/**
 * @author flin
 */
public class TestableClassTransformer implements ClassFileTransformer {

    private final Set<ComparableWeakRef<String>> loadedClassNames = ComparableWeakRef.getWeekHashSet();

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        if (isSystemClass(loader, className) || loadedClassNames.contains(new ComparableWeakRef<String>(className))) {
            // Ignore system class and reloaded class
            return null;
        }
        try {
            if (shouldTransformAsSourceClass(className)) {
                // it's a source class with testable enabled
                loadedClassNames.add(new ComparableWeakRef<String>(className));
                List<MethodInfo> injectMethods = getTestableMockMethods(ClassUtil.getTestClassName(className));
                return new SourceClassHandler(injectMethods).getBytes(classFileBuffer);
            } else if (shouldTransformAsTestClass(className)) {
                // it's a test class with testable enabled
                loadedClassNames.add(new ComparableWeakRef<String>(className));
                return new TestClassHandler().getBytes(classFileBuffer);
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    private boolean shouldTransformAsSourceClass(String className) {
        return ClassUtil.anyMethodHasAnnotation(ClassUtil.getTestClassName(className), ConstPool.TESTABLE_MOCK);
    }

    private boolean shouldTransformAsTestClass(String className) {
        return className.endsWith(ConstPool.TEST_POSTFIX) &&
            ClassUtil.anyMethodHasAnnotation(className, ConstPool.TESTABLE_MOCK);
    }

    private boolean isSystemClass(ClassLoader loader, String className) {
        // className can be null for Java 8 lambdas
        return !(loader instanceof URLClassLoader) || null == className || className.startsWith("jdk/");
    }

    private List<MethodInfo> getTestableMockMethods(String className) {
        try {
            List<MethodInfo> methodInfos = new ArrayList<MethodInfo>();
            ClassNode cn = new ClassNode();
            new ClassReader(className).accept(cn, 0);
            for (MethodNode mn : cn.methods) {
                checkMethodAnnotation(cn, methodInfos, mn);
            }
            return methodInfos;
        } catch (Exception e) {
            return new ArrayList<MethodInfo>();
        }
    }

    private void checkMethodAnnotation(ClassNode cn, List<MethodInfo> methodInfos, MethodNode mn) {
        ImmutablePair<String, String> methodDescPair = extractFirstParameter(mn.desc);
        if (methodDescPair == null || mn.visibleAnnotations == null) {
            return;
        }
        for (AnnotationNode an : mn.visibleAnnotations) {
            if (toDotSeparateFullClassName(an.desc).equals(ConstPool.TESTABLE_MOCK)) {
                String targetClass = ClassUtil.toSlashSeparateFullClassName(methodDescPair.left);
                String targetMethod = AnnotationUtil.getAnnotationParameter(an, ConstPool.FIELD_TARGET_METHOD, mn.name);
                if (targetMethod.equals(ConstPool.CONSTRUCTOR)) {
                    String sourceClassName = ClassUtil.getSourceClassName(cn.name);
                    methodInfos.add(new MethodInfo(sourceClassName, targetMethod, mn.name, mn.desc));
                } else {
                    methodInfos.add(new MethodInfo(targetClass, targetMethod, mn.name, methodDescPair.right));
                }
                break;
            }
        }
    }

    /**
     * Split desc to "first parameter" and "desc of rest parameters"
     * @param desc method desc
     */
    private ImmutablePair<String, String> extractFirstParameter(String desc) {
        // assume first parameter is a class
        int pos = desc.indexOf(";");
        return pos < 0 ? null : ImmutablePair.of(desc.substring(1, pos + 1), "(" + desc.substring(pos + 1));
    }

}
