package com.alibaba.testable.core.util;

import com.alibaba.testable.core.compile.InMemoryJavaCompiler;

import java.lang.reflect.Method;

import static com.alibaba.testable.core.constant.ConstPool.DOLLAR;
import static com.alibaba.testable.core.constant.ConstPool.DOT;

public class ConstructionUtil {

    private static final String TESTABLE_IMPL = "$TestableImpl";

    public static <T> T generateSubClassOf(Class<T> clazz) throws InstantiationException {
        StringBuilder sourceCode = new StringBuilder();
        sourceCode.append("package ").append(clazz.getPackage().getName()).append(";\n")
                .append("public class ").append(getSubclassName(clazz))
                .append(clazz.isInterface() ? " implements " : " extends ")
                .append(clazz.getName().replace(DOLLAR, DOT))
                .append(" {\n");
        for (Method m : clazz.getMethods()) {

        }
        sourceCode.append("}");

        try {
            return (T) InMemoryJavaCompiler.newInstance()
                    .useParentClassLoader(clazz.getClassLoader())
                    .compile(clazz.getPackage().getName() + DOT + getSubclassName(clazz), sourceCode.toString())
                    .newInstance();
        } catch (Exception e) {
            throw new InstantiationException(e.toString());
        }
    }

    private static String getSubclassName(Class<?> clazz) {
        return clazz.getSimpleName() + TESTABLE_IMPL;
    }

}
