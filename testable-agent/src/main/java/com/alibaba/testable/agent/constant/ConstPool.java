package com.alibaba.testable.agent.constant;

/**
 * @author flin
 */
public class ConstPool {

    public static final String FIELD_TARGET_METHOD = "targetMethod";
    public static final String FIELD_TARGET_CLASS = "targetClass";
    public static final String FIELD_SCOPE = "scope";

    public static final String PROPERTY_USER_DIR = "user.dir";
    public static final String PROPERTY_TEMP_DIR = "java.io.tmpdir";

    public static final String MOCK_WITH = "com.alibaba.testable.core.annotation.MockWith";
    public static final String DUMP_TO = "com.alibaba.testable.core.annotation.DumpTo";
    public static final String MOCK_DIAGNOSE = "com.alibaba.testable.core.annotation.MockDiagnose";
    public static final String MOCK_METHOD = "com.alibaba.testable.core.annotation.MockMethod";
    public static final String MOCK_CONSTRUCTOR = "com.alibaba.testable.core.annotation.MockConstructor";

    public static final String CGLIB_CLASS_PATTERN = "$$EnhancerBy";
    public static final String KOTLIN_POSTFIX_COMPANION = "$Companion";
    public static final String KOTLIN_PREFIX_ACCESS = "access$";

    public static final String CLASS_OBJECT = "java/lang/Object";
}
