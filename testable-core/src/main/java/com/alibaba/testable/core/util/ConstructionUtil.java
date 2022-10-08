package com.alibaba.testable.core.util;

import com.alibaba.testable.core.compile.InMemoryJavaCompiler;
import com.alibaba.testable.core.tool.OmniConstructor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

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
            if (!Modifier.isStatic(m.getModifiers()) && !Modifier.isFinal(m.getModifiers())) {
                sourceCode.append("\tpublic ").append(m.getReturnType().getName().replace(DOLLAR, DOT)).append(" ")
                        .append(m.getName()).append("(");
                Class<?>[] parameters = m.getParameterTypes();
                for (int i = 0; i < parameters.length; i++) {
                    sourceCode.append(getParameterName(parameters[i])).append(" p").append(i);
                    if (i < parameters.length - 1) {
                        sourceCode.append(", ");
                    }
                }
                sourceCode.append(") {\n");
                if (!m.getReturnType().equals(void.class)) {
                    sourceCode.append("\t\treturn ").append(OmniConstructor.class.getName().replace(DOLLAR, DOT)).append(".")
                            .append("newInstance(").append(m.getReturnType().getName().replace(DOLLAR, DOT)).append(".class);\n");
                }
                sourceCode.append("\t}\n");
            }
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

    private static String getParameterName(Class<?> parameter) {
        if (parameter.isArray()) {
            return getParameterName(parameter.getComponentType()) + "[]";
        }
        return parameter.getName().replace(DOLLAR, DOT);
    }

    private static String getSubclassName(Class<?> clazz) {
        return clazz.getSimpleName() + TESTABLE_IMPL;
    }

}
