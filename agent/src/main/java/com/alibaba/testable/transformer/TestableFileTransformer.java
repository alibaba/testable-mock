package com.alibaba.testable.transformer;

import com.alibaba.testable.util.ClassUtil;

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
public class TestableFileTransformer implements ClassFileTransformer {

    private static final String ENABLE_TESTABLE = "com.alibaba.testable.annotation.EnableTestable";
    private static final String ENABLE_TESTABLE_INJECT = "com.alibaba.testable.annotation.EnableTestableInject";
    private static final String TEST_POSTFIX = "Test";

    private static final Set<String> loadedClassNames = new HashSet<String>();

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (isSystemClass(loader, className)) {
            // Ignore system class
            return null;
        }

        List<String> annotations = ClassUtil.getAnnotations(className);
        List<String> testAnnotations = ClassUtil.getAnnotations(className + TEST_POSTFIX);
        if (!isNeedTransform(annotations, testAnnotations)) {
            // Neither EnableTestable on test class, nor EnableTestableInject on source class
            return null;
        }

        try {
            return new TestableClassTransformer(className).getBytes();
        } catch (IOException e) {
            return null;
        }
    }

    private boolean isSystemClass(ClassLoader loader, String className) {
        return !(loader instanceof URLClassLoader) || null == className || className.startsWith("jdk/");
    }

    private boolean isNeedTransform(List<String> annotations, List<String> testAnnotations) {
        return annotations != null &&
            (annotations.contains(ENABLE_TESTABLE_INJECT) ||
                (testAnnotations != null && testAnnotations.contains(ENABLE_TESTABLE)));
    }

}
