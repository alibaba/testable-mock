package com.alibaba.testable.agent.transformer;

import com.alibaba.testable.agent.constant.ConstPool;
import com.alibaba.testable.agent.handler.SourceClassHandler;
import com.alibaba.testable.agent.handler.TestClassHandler;
import com.alibaba.testable.agent.util.ClassUtil;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author flin
 */
public class TestableClassTransformer implements ClassFileTransformer {

    private static final String ENABLE_TESTABLE = "com.alibaba.testable.core.annotation.EnableTestable";
    private static final String ENABLE_TESTABLE_INJECT = "com.alibaba.testable.core.annotation.EnableTestableInject";

    private static final Set<String> loadedClassNames = new HashSet<String>();

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        if (isSystemClass(loader, className) || loadedClassNames.contains(className)) {
            // Ignore system class and duplicate class
            return null;
        }

        List<String> annotations = ClassUtil.getAnnotations(className);
        List<String> testAnnotations = ClassUtil.getAnnotations(className + ConstPool.TEST_POSTFIX);
        try {
            if (annotations.contains(ENABLE_TESTABLE_INJECT) || testAnnotations.contains(ENABLE_TESTABLE)) {
                loadedClassNames.add(className);
                return new SourceClassHandler().getBytes(className);
            } else if (annotations.contains(ENABLE_TESTABLE)) {
                loadedClassNames.add(className);
                return new TestClassHandler().getBytes(className);
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    private boolean isSystemClass(ClassLoader loader, String className) {
        return !(loader instanceof URLClassLoader) || null == className || className.startsWith("jdk/");
    }

}
