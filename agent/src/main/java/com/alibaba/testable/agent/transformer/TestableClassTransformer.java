package com.alibaba.testable.agent.transformer;

import com.alibaba.testable.agent.constant.ConstPool;
import com.alibaba.testable.agent.handler.SourceClassHandler;
import com.alibaba.testable.agent.handler.TestClassHandler;
import com.alibaba.testable.agent.model.MethodInfo;
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

    private final Set<String> loadedClassNames = new HashSet<String>();

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        if (isSystemClass(loader, className) || loadedClassNames.contains(className)) {
            // Ignore system class and duplicate class
            return null;
        }

        List<String> annotations = ClassUtil.getAnnotations(className);
        List<String> testAnnotations = ClassUtil.getAnnotations(className + ConstPool.TEST_POSTFIX);
        try {
            if (testAnnotations.contains(ConstPool.ENABLE_TESTABLE)) {
                loadedClassNames.add(className);
                List<MethodInfo> injectMethods = ClassUtil.getTestableInjectMethods(className + ConstPool.TEST_POSTFIX);
                return new SourceClassHandler(injectMethods).getBytes(className);
            } else if (annotations.contains(ConstPool.ENABLE_TESTABLE)) {
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
