package com.alibaba.testable.util;

/**
 * @author flin
 */
public final class ConstPool {

    public static final String NE_PKG = "n";
    public static final String NE_CLS = "e";
    public static final String NE_NEW = "w";
    public static final String NE_FUN = "f";
    public static final String NE_PKG_CLS = NE_PKG + ".e";
    public static final String NE_POOL = NE_PKG_CLS + ".p";
    public static final String NE_ADD_W = NE_PKG_CLS + ".aw";
    public static final String NE_ADD_F = NE_PKG_CLS + ".af";
    public static final String TYPE_TO_CLASS = "class";
    public static final String REF_THIS = "this";
    public static final String VOID = "void";
    public static final String TESTABLE_PRIVATE_ACCESSOR = "com.alibaba.testable.accessor.PrivateAccessor";
    public static final String ANNOTATION_TESTABLE_INJECT = "com.alibaba.testable.annotation.TestableInject";
    public static final String ANNOTATION_JUNIT5_SETUP = "org.junit.jupiter.api.BeforeEach";
    public static final String ANNOTATION_JUNIT5_TEST = "org.junit.jupiter.api.Test";
}
