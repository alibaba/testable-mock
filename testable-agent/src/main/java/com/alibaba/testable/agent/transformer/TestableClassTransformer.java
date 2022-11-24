package com.alibaba.testable.agent.transformer;

import com.alibaba.testable.agent.constant.ConstPool;
import com.alibaba.testable.agent.handler.*;
import com.alibaba.testable.agent.handler.test.Framework;
import com.alibaba.testable.agent.model.MethodInfo;
import com.alibaba.testable.agent.util.*;
import com.alibaba.testable.core.exception.TargetNotExistException;
import com.alibaba.testable.core.model.ClassType;
import com.alibaba.testable.core.util.LogUtil;
import com.alibaba.testable.core.util.MockAssociationUtil;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;

import javax.lang.model.type.NullType;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.List;

import static com.alibaba.testable.agent.constant.ConstPool.*;
import static com.alibaba.testable.core.constant.ConstPool.DOLLAR;
import static com.alibaba.testable.core.constant.ConstPool.TEST_POSTFIX;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * @author flin
 */
public class TestableClassTransformer implements ClassFileTransformer {

    private static final String FIELD_TREAT_AS = "treatAs";
    private static final String CLASS_JUNIT_5_NESTED = "org.junit.jupiter.api.Nested";

    /**
     * Just avoid spend time to scan those surely non-user classes, should keep these lists as tiny as possible
     */
    private final String[] BLACKLIST_PREFIXES = new String[] { "sun/", "com/sun/", "javax/crypto/",
        "java/util/logging/", "org/gradle/", "org/robolectric/" };

    private final MockClassParser mockClassParser = new MockClassParser();
    private final TestClassChecker testClassChecker = new TestClassChecker();

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        // className is in slash-separated format
        if (isSystemClass(className)) {
            // Ignore system class and reloaded class
            return null;
        }
        byte[] bytes = GlobalConfig.enhanceOmniConstructor ?
            new OmniClassHandler().getBytes(classFileBuffer) : classFileBuffer;
        bytes = GlobalConfig.enhanceFinal ? new FinalFieldClassHandler().getBytes(bytes) : bytes;
        if (GlobalConfig.enhanceMock) {
            ClassNode cn = ClassUtil.getClassNode(className);
            if (cn != null) {
                return transformMock(bytes, cn);
            }
        }
        return bytes;
    }

    private byte[] transformMock(byte[] bytes, ClassNode cn) {
        try {
            if (mockClassParser.isMockClass(cn)) {
                // it's a mock class
                bytes = new MockClassHandler(cn.name).getBytes(bytes);
                BytecodeUtil.dumpByte(cn, GlobalConfig.getDumpPath(), bytes);
                return bytes;
            }
            String mockClass = foundMockForSourceClass(cn.name);
            if (mockClass != null) {
                // it's a source class with testable enabled
                List<MethodInfo> injectMethods = mockClassParser.getTestableMockMethods(mockClass);
                bytes = new SourceClassHandler(injectMethods, mockClass).getBytes(bytes);
                BytecodeUtil.dumpByte(cn, GlobalConfig.getDumpPath(), bytes);
                return bytes;
            }
            Framework framework = testClassChecker.checkFramework(cn);
            if (framework != null) {
                // it's a test class
                bytes = new TestClassHandler(framework).getBytes(bytes);
                BytecodeUtil.dumpByte(cn, GlobalConfig.getDumpPath(), bytes);
                return bytes;
            } else if (cn.name.endsWith(TEST_POSTFIX)) {
                LogUtil.verbose("Failed to detect test framework for %s", cn.name);
            }
        } catch (TargetNotExistException e) {
            LogUtil.error("Invalid mock method %s::%s - %s", e.getClassName(), e.getMethodName(), e.getMessage());
            System.exit(0);
        } catch (Throwable t) {
            LogUtil.warn("Failed to transform class " + cn.name);
            LogUtil.warn(t.toString());
            LogUtil.warn(ThreadUtil.getFirstRelatedStackLine(t));
        } finally {
            LogUtil.resetLogLevel();
        }
        BytecodeUtil.dumpByte(cn, null, bytes);
        return bytes;
    }

    private String foundMockForSourceClass(String name) {
        String className = (GlobalConfig.getMockPackageMapping() == null) ? name : mapPackage(name);
        // handle @MockWith annotation on source class
        String mockClass = lookForMockWithAnnotationAsSourceClass(className);
        if (mockClass != null) {
            return mockClass;
        }
        // look for [ThisClass]Test.Mock, [ThisClass]Mock and class with @MockContainer annotation
        mockClass = foundMockForStandardClass(className);
        if (mockClass != null) {
            return mockClass;
        }
        // inner class should also look for mock class in the test class of its outer class
        return foundMockForInnerSourceClass(className);
    }

    private String mapPackage(String name) {
        String dotSeparatedName = ClassUtil.toDotSeparatedName(name);
        for (String prefix : GlobalConfig.getMockPackageMapping().keySet()) {
            if (dotSeparatedName.startsWith(prefix)) {
                return ClassUtil.toSlashSeparatedName(GlobalConfig.getMockPackageMapping().get(prefix))
                        + name.substring(prefix.length());
            }
        }
        return name;
    }

    private String foundMockForInnerSourceClass(String className) {
        return (className.contains(DOLLAR) && !className.endsWith(KOTLIN_POSTFIX_COMPANION)) ?
            foundMockForStandardClass(className.substring(0, className.indexOf(DOLLAR))) : null;
    }

    private String foundMockForStandardClass(String className) {
        ClassNode cn = adaptInnerClass(ClassUtil.getClassNode(ClassUtil.getTestClassName(className)));
        if (cn != null) {
            // handle @MockWith annotation on test class
            String mockClass = lookForMockWithAnnotationAsTestClass(cn);
            if (mockClass != null) {
                return mockClass;
            }
            // look for [ThisClass]Test.Mock or @MockContainer annotation
            mockClass = lookForInnerMockClass(cn);
            if (mockClass != null) {
                return mockClass;
            }
        }
        // look for [ThisClass]Mock
        return lookForOuterMockClass(className);
    }

    private ClassNode adaptInnerClass(ClassNode cn) {
        if (AnnotationUtil.getClassAnnotation(cn, CLASS_JUNIT_5_NESTED) != null) {
            return ClassUtil.getClassNode(ClassUtil.toOuterClassName(cn.name));
        }
        return cn;
    }

    private String lookForOuterMockClass(String className) {
        String mockClassName = ClassUtil.getMockClassName(ClassUtil.getSourceClassName(className));
        if (mockClassParser.isMockClass(ClassUtil.getClassNode(mockClassName))) {
            return mockClassName;
        }
        return null;
    }

    private boolean isSystemClass(String className) {
        // className can be null for Java 8 lambdas
        if (null == className || className.contains(CGLIB_CLASS_PATTERN)) {
            return true;
        }
        String[] blackList = GlobalConfig.getPkgPrefixBlackList();
        if (blackList != null && isInPrefixList(className, blackList)) {
            return true;
        }
        String[] whiteList = GlobalConfig.getPkgPrefixWhiteList();
        if (whiteList != null) {
            // Only consider package in provided list as non-system class
            return !isInPrefixList(className, whiteList);
        }
        return isInPrefixList(className, BLACKLIST_PREFIXES);
    }

    private boolean isInPrefixList(String name, String[] prefixList) {
        for (String prefix : prefixList) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Read @MockWith annotation upon class to fetch mock class
     *
     * @param className class that need to explore
     * @return name of mock class, null for not found
     */
    private String lookForMockWithAnnotationAsSourceClass(String className) {
        ClassNode cn = ClassUtil.getClassNode(className);
        if (cn == null) {
            return null;
        }
        return parseMockWithAnnotation(cn, ClassType.SourceClass);
    }

    /**
     * Read inner class "Mock" to fetch mock class
     *
     * @param cn class that need to explore
     * @return name of mock class, null for not found
     */
    private String lookForInnerMockClass(ClassNode cn) {
        for (InnerClassNode ic : cn.innerClasses) {
            ClassNode innerClassNode = ClassUtil.getClassNode(ic.name);
            boolean isNameMatched = ic.name.equals(getInnerMockClassName(cn.name)) ||
                    AnnotationUtil.getClassAnnotation(innerClassNode, MOCK_CONTAINER) != null;
            if (isNameMatched && mockClassParser.isMockClass(innerClassNode)) {
                if ((ic.access & ACC_STATIC) == 0) {
                    LogUtil.warn("Mock class in \"%s\" is not declared as static", cn.name);
                } else {
                    ic.access = BytecodeUtil.toPublicAccess(ic.access);
                    return ic.name;
                }
            }
        }
        return null;
    }

    /**
     * Read @MockWith annotation upon class to fetch mock class
     *
     * @param cn class that need to explore
     * @return name of mock class, null for not found
     */
    private String lookForMockWithAnnotationAsTestClass(ClassNode cn) {
        String mockClassName = parseMockWithAnnotation(cn, ClassType.TestClass);
        if (mockClassName != null) {
            MockAssociationUtil.mockToTests.get(mockClassName).add(ClassUtil.toJavaStyleClassName(cn.name));
            return ClassUtil.toSlashSeparatedName(mockClassName);
        }
        return null;
    }

    /**
     * Get mock class from @MockWith annotation
     *
     * @param cn class that may have @MockWith annotation
     * @return mock class name
     */
    private String parseMockWithAnnotation(ClassNode cn, ClassType expectedType) {
        AnnotationNode an = AnnotationUtil.getClassAnnotation(cn, MOCK_WITH);
        if (an != null) {
            ClassType type = AnnotationUtil.getAnnotationParameter(an, FIELD_TREAT_AS, ClassType.GuessByName,
                ClassType.class);
            if (isExpectedType(cn.name, type, expectedType)) {
                Type clazz = AnnotationUtil.getAnnotationParameter(an, ConstPool.FIELD_VALUE,
                    Type.getType(NullType.class), Type.class);
                DiagnoseUtil.setupByClass(ClassUtil.getClassNode(clazz.getClassName()));
                return clazz.getClassName();
            }
        }
        return null;
    }

    private boolean isExpectedType(String className, ClassType type, ClassType expectedType) {
        if (type.equals(ClassType.GuessByName)) {
            return expectedType.equals(ClassType.TestClass) == className.endsWith(TEST_POSTFIX);
        } else {
            return type.equals(expectedType);
        }
    }

    private String getInnerMockClassName(String className) {
        return className + DOLLAR + GlobalConfig.innerMockClassName;
    }

}
