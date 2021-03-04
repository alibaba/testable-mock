package com.alibaba.testable.agent.transformer;

import com.alibaba.testable.agent.constant.ConstPool;
import com.alibaba.testable.agent.handler.MockClassHandler;
import com.alibaba.testable.agent.handler.SourceClassHandler;
import com.alibaba.testable.agent.handler.TestClassHandler;
import com.alibaba.testable.agent.model.MethodInfo;
import com.alibaba.testable.agent.util.*;
import com.alibaba.testable.core.model.ClassType;
import com.alibaba.testable.core.util.LogUtil;
import com.alibaba.testable.core.util.MockAssociationUtil;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;

import javax.lang.model.type.NullType;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.List;

import static com.alibaba.testable.agent.constant.ConstPool.*;
import static com.alibaba.testable.agent.util.ClassUtil.toDotSeparateFullClassName;
import static com.alibaba.testable.core.constant.ConstPool.TEST_POSTFIX;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * @author flin
 */
public class TestableClassTransformer implements ClassFileTransformer {

    private static final String FIELD_VALUE = "value";
    private static final String FIELD_TREAT_AS = "treatAs";
    private static final String COMMA = ",";
    private static final String CLASS_NAME_MOCK = "Mock";

    /**
     * Just avoid spend time to scan those surely non-user classes Should keep these lists as tiny as possible
     */
    private final String[] BLACKLIST_PREFIXES = new String[] {"jdk/", "java/", "javax/", "sun/", "com/sun/",
        "org/apache/maven/", "com/alibaba/testable/", "junit/", "org/junit/", "org/testng/"};

    public MockClassParser mockClassParser = new MockClassParser();

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        if (isSystemClass(className)) {
            // Ignore system class and reloaded class
            return null;
        }
        LogUtil.verbose("Handle class: " + className);
        byte[] bytes = null;
        try {
            if (mockClassParser.isMockClass(className)) {
                // it's a mock class
                LogUtil.diagnose("Handling mock class %s", className);
                bytes = new MockClassHandler(className).getBytes(classFileBuffer);
                dumpByte(className, bytes);
            } else {
                String mockClass = foundMockForTestClass(className);
                if (mockClass != null) {
                    // it's a test class with testable enabled
                    LogUtil.diagnose("Handling test class %s", className);
                    bytes = new TestClassHandler(mockClass).getBytes(classFileBuffer);
                    dumpByte(className, bytes);
                } else {
                    mockClass = foundMockForSourceClass(className);
                    if (mockClass != null) {
                        // it's a source class with testable enabled
                        List<MethodInfo> injectMethods = mockClassParser.getTestableMockMethods(mockClass);
                        LogUtil.diagnose("Handling source class %s", className);
                        bytes = new SourceClassHandler(injectMethods, mockClass).getBytes(classFileBuffer);
                        dumpByte(className, bytes);
                    }
                }
            }
        } catch (Throwable t) {
            LogUtil.warn("Failed to transform class " + className);
            LogUtil.diagnose(t.toString());
            LogUtil.diagnose(ThreadUtil.getFirstRelatedStackLine(t));
        } finally {
            LogUtil.resetLogLevel();
        }
        return bytes;
    }

    private void dumpByte(String className, byte[] bytes) {
        String dumpDir = GlobalConfig.getDumpPath();
        if (dumpDir == null || dumpDir.isEmpty() || !new File(dumpDir).isDirectory()) {
            return;
        }
        try {
            String dumpFile = StringUtil.joinPath(dumpDir,
                className.replace(SLASH, DOT).replace(DOLLAR, UNDERLINE) + ".class");
            LogUtil.verbose("Dump class: " + dumpFile);
            FileOutputStream stream = new FileOutputStream(dumpFile);
            stream.write(bytes);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String foundMockForSourceClass(String className) {
        String mockClass = lookForMockWithAnnotationAsSourceClass(className);
        if (mockClass != null) {
            return mockClass;
        }
        mockClass = foundMockForTestClass(ClassUtil.getTestClassName(className));
        if (mockClass != null) {
            return mockClass;
        }
        return foundMockForInnerSourceClass(className);
    }

    private String foundMockForInnerSourceClass(String className) {
        return (className.contains(DOLLAR) && !className.endsWith(KOTLIN_POSTFIX_COMPANION)) ?
            foundMockForTestClass(ClassUtil.getTestClassName(className.substring(0, className.indexOf(DOLLAR)))) : null;
    }

    private String foundMockForTestClass(String className) {
        ClassNode cn = ClassUtil.getClassNode(className);
        if (cn != null) {
            String mockClass = lookForMockWithAnnotationAsTestClass(cn);
            if (mockClass != null) {
                return mockClass;
            }
            mockClass = lookForInnerMockClass(cn);
            if (mockClass != null) {
                return mockClass;
            }
        }
        return lookForOuterMockClass(className);
    }

    private String lookForOuterMockClass(String className) {
        String mockClass;
        mockClass = ClassUtil.getMockClassName(ClassUtil.getSourceClassName(className));
        if (mockClassParser.isMockClass(mockClass)) {
            return mockClass;
        }
        return null;
    }

    private boolean isSystemClass(String className) {
        // className can be null for Java 8 lambdas
        if (null == className) {
            return true;
        }
        String whitePrefix = GlobalConfig.getPkgPrefix();
        if (whitePrefix != null) {
            for (String prefix : whitePrefix.split(COMMA)) {
                if (className.startsWith(prefix)) {
                    // Only consider package in provided list as non-system class
                    return false;
                }
            }
            return true;
        } else {
            for (String prefix : BLACKLIST_PREFIXES) {
                if (className.startsWith(prefix)) {
                    return true;
                }
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
            if ((ic.access & ACC_PUBLIC) != 0 && ic.name.equals(getInnerMockClassName(cn.name)) &&
                mockClassParser.isMockClass(ic.name)) {
                if ((ic.access & ACC_STATIC) != 0) {
                    return ic.name;
                } else {
                    LogUtil.warn(String.format("Mock class in \"%s\" is not declared as static", cn.name));
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
            MockAssociationUtil.mockToTests.get(mockClassName).add(ClassUtil.toDotSeparateFullClassName(cn.name));
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
        if (cn.visibleAnnotations != null) {
            for (AnnotationNode an : cn.visibleAnnotations) {
                DiagnoseUtil.setupByAnnotation(an);
                if (toDotSeparateFullClassName(an.desc).equals(ConstPool.MOCK_WITH)) {
                    ClassType type = AnnotationUtil.getAnnotationParameter(an, FIELD_TREAT_AS, ClassType.GuessByName,
                        ClassType.class);
                    if (isExpectedType(cn.name, type, expectedType)) {
                        Type clazz = AnnotationUtil.getAnnotationParameter(an, FIELD_VALUE,
                            Type.getType(NullType.class), Type.class);
                        DiagnoseUtil.setupByClass(ClassUtil.getClassNode(clazz.getClassName()));
                        return clazz.getClassName();
                    }
                }
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
        return className + DOLLAR + CLASS_NAME_MOCK;
    }

}
