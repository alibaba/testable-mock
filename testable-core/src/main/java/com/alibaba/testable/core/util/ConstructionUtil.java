package com.alibaba.testable.core.util;

import com.alibaba.testable.core.compile.InMemoryJavaCompiler;

import java.lang.reflect.Method;

public class ConstructionUtil {

    private static final String TESTABLE_IMPL = "$TestableImpl";
    private static final String TESTABLE_OMNI_PKG = "com.alibaba.testable.omni";

    public static <T> T generateSubClassOf(Class<T> clazz) throws InstantiationException {
        StringBuilder sourceCode = new StringBuilder();
        sourceCode.append("package ").append(TESTABLE_OMNI_PKG).append(";\n")
                .append("public class ").append(getSubclassName(clazz))
                .append(clazz.isInterface() ? " implements " : " extends ").append(clazz.getName()).append(" {\n");
        for (Method m : clazz.getMethods()) {

        }
        sourceCode.append("}");

        try {
            Class<?> helloClass = InMemoryJavaCompiler.newInstance()
                    .compile(TESTABLE_OMNI_PKG + "." + getSubclassName(clazz), sourceCode.toString());
            return (T) helloClass.newInstance();
        } catch (Exception e) {
            throw new InstantiationException(e.toString());
        }
    }

    private static String getSubclassName(Class<?> clazz) {
        return clazz.getSimpleName() + TESTABLE_IMPL;
    }

}
