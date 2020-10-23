package com.alibaba.testable.agent.transformer;

import com.alibaba.testable.agent.constant.ConstPool;
import com.alibaba.testable.agent.handler.SourceClassHandler;
import com.alibaba.testable.agent.handler.TestClassHandler;
import com.alibaba.testable.agent.model.ImmutablePair;
import com.alibaba.testable.agent.model.MethodInfo;
import com.alibaba.testable.agent.tool.ComparableWeakRef;
import com.alibaba.testable.agent.util.ClassUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
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
import static com.alibaba.testable.agent.util.ClassUtil.toSlashSeparatedName;

/**
 * @author flin
 */
public class TestableClassTransformer implements ClassFileTransformer {

    private final Set<ComparableWeakRef<String>> loadedClassNames = ComparableWeakRef.getWeekHashSet();
    private static final String TARGET_CLASS = "targetClass";
    private static final String TARGET_METHOD = "targetMethod";

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
                List<MethodInfo> injectMethods = getTestableInjectMethods(ClassUtil.getTestClassName(className));
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
        return ClassUtil.anyMethodHasAnnotation(ClassUtil.getTestClassName(className), ConstPool.TESTABLE_INJECT);
    }

    private boolean shouldTransformAsTestClass(String className) {
        return className.endsWith(ConstPool.TEST_POSTFIX) &&
            ClassUtil.anyMethodHasAnnotation(className, ConstPool.TESTABLE_INJECT);
    }

    private boolean isSystemClass(ClassLoader loader, String className) {
        return !(loader instanceof URLClassLoader) || null == className || className.startsWith("jdk/");
    }

    private List<MethodInfo> getTestableInjectMethods(String className) {
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
        if (mn.visibleAnnotations == null) {
            return;
        }
        for (AnnotationNode an : mn.visibleAnnotations) {
            if (toDotSeparateFullClassName(an.desc).equals(ConstPool.TESTABLE_INJECT)) {
                String sourceClassName = ClassUtil.getSourceClassName(cn.name);
                String targetClass = getAnnotationParameter(an, TARGET_CLASS, sourceClassName);
                String targetMethod = getAnnotationParameter(an, TARGET_METHOD, mn.name);
                if (sourceClassName.equals(targetClass)) {
                    // member method of the source class
                    methodInfos.add(new MethodInfo(
                        toSlashSeparatedName(targetClass), targetMethod, null, mn.desc));
                } else {
                    // member method of a common class
                    ImmutablePair<String, String> methodDescPair = extractFirstParameter(mn.desc);
                    if (methodDescPair != null && methodDescPair.left.equals(ClassUtil.toByteCodeClassName(targetClass))) {
                        methodInfos.add(new MethodInfo(
                            toSlashSeparatedName(targetClass), targetMethod, mn.name, methodDescPair.right));
                    }
                }
                break;
            }
        }
    }

    private ImmutablePair<String, String> extractFirstParameter(String desc) {
        // assume first parameter is a class
        int pos = desc.indexOf(";");
        return pos < 0 ? null : ImmutablePair.of(desc.substring(1, pos + 1), "(" + desc.substring(pos + 1));
    }

    private String getAnnotationParameter(AnnotationNode an, String key, String defaultValue) {
        if (an.values != null) {
            int i = an.values.indexOf(key);
            if (i % 2 == 0) {
                Object value = an.values.get(i + 1);
                if (value instanceof Type) {
                    // fit for `targetClass` parameter
                    return ClassUtil.toSlashSeparateFullClassName(value.toString());
                }
                return value.toString();
            }
        }
        return defaultValue;
    }

}
