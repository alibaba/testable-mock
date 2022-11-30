package com.alibaba.testable.core.util;

import com.alibaba.testable.core.compile.InMemoryJavaCompiler;
import com.alibaba.testable.core.tool.OmniConstructor;

import java.lang.reflect.*;
import java.util.*;

import static com.alibaba.testable.core.constant.ConstPool.DOT;

public class ConstructionUtil {

    private static final String TESTABLE_IMPL = "$TestableImpl";

    public static <T> T generateSubClassOf(Class<T> clazz) throws InstantiationException {
        StringBuilder sourceCode = new StringBuilder();
        String packageName = adaptName(clazz.getPackage().getName());
        Map<String, String> genericNames = getImplicitGenericParameters(clazz);
        sourceCode.append("package ")
                .append(packageName)
                .append(";\npublic class ")
                .append(getSubclassName(clazz))
                .append(getTypeParameters(clazz.getTypeParameters(), true, genericNames))
                .append(clazz.isInterface() ? " implements " : " extends ")
                .append(getClassName(clazz, genericNames))
                .append(getTypeParameters(clazz.getTypeParameters(), false, genericNames))
                .append(" {\n");
        for (Method m : clazz.getMethods()) {
            if (!Modifier.isStatic(m.getModifiers()) && !Modifier.isFinal(m.getModifiers())) {
                sourceCode.append("\tpublic ")
                        .append(getTypeParameters(m.getTypeParameters(), true, genericNames))
                        .append(getClassName(m.getGenericReturnType(), genericNames))
                        .append(" ").append(m.getName()).append("(");
                Type[] parameters = m.getGenericParameterTypes();
                for (int i = 0; i < parameters.length; i++) {
                    sourceCode.append(getParameterName(parameters[i], genericNames)).append(" p").append(i);
                    if (i < parameters.length - 1) {
                        sourceCode.append(", ");
                    }
                }
                sourceCode.append(") {\n");
                if (!m.getReturnType().equals(void.class)) {
                    sourceCode.append("\t\treturn (").append(getClassName(m.getGenericReturnType(), genericNames))
                            .append(") ")
                            .append(getClassName(OmniConstructor.class, genericNames))
                            .append(".")
                            .append("newInstance(").append(getClassName(m.getReturnType(), genericNames))
                            .append(".class);\n");
                }
                sourceCode.append("\t}\n");
            }
        }
        sourceCode.append("}");

        try {
            return (T) InMemoryJavaCompiler.newInstance()
                    .useParentClassLoader(clazz.getClassLoader())
                    .useOptions("-Xlint:unchecked")
                    .ignoreWarnings()
                    .compile(packageName + DOT + getSubclassName(clazz), sourceCode.toString())
                    .newInstance();
        } catch (Exception e) {
            throw new InstantiationException(e.toString());
        }
    }

    private static String adaptName(String name) {
        // create class in 'java' package will cause 'prohibited package name' error
        return name.replaceAll("^java\\.", "testable.");
    }

    private static <T> Map<String, String> getImplicitGenericParameters(Class<T> clazz) {
        Map<String, String> templateTypeMap = new HashMap<String, String>();
        List<Type> superTypes = new ArrayList<Type>(Arrays.asList(clazz.getGenericInterfaces()));
        if (clazz.getGenericSuperclass() != null) {
            superTypes.add(clazz.getGenericSuperclass());
        }
        for (Type t : superTypes) {
            if (t instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType)t;
                Type[] actualTypeArguments = pt.getActualTypeArguments();
                TypeVariable<? extends Class<?>>[] rawTypedParameters = ((Class<?>) pt.getRawType()).getTypeParameters();
                for (int i = 0; i < actualTypeArguments.length; i++) {
                    templateTypeMap.put(getClassName(rawTypedParameters[i]), getClassName(actualTypeArguments[i]));
                }
            }
        }
        return templateTypeMap;
    }

    private static String getTypeParameters(TypeVariable<?>[] typeParameters, boolean withScope,
                                            Map<String, String> genericNames) {
        if (typeParameters.length > 0) {
            StringBuilder sb = new StringBuilder("<");
            for (int i = 0; i < typeParameters.length; i++) {
                sb.append(typeParameters[i].getName());
                if (withScope) {
                    sb.append(" extends ");
                    Type[] bounds = typeParameters[i].getBounds();
                    for (int j = 0; j < bounds.length; j++) {
                        sb.append(getClassName(bounds[j], genericNames));
                        if (j < bounds.length - 1) {
                            sb.append(" & ");
                        }
                    }
                }
                if (i < typeParameters.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("> ");
            return sb.toString();
        }
        return "";
    }

    private static String getTypeParameters(Type[] typeParameters, Map<String, String> genericNames) {
        if (typeParameters.length > 0) {
            StringBuilder sb = new StringBuilder("<");
            for (int i = 0; i < typeParameters.length; i++) {
                sb.append(getClassName(typeParameters[i], genericNames));
                if (i < typeParameters.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("> ");
            return sb.toString();
        }
        return "";
    }

    private static String getWildcardType(WildcardType wildcardType, Map<String, String> genericNames) {
        StringBuilder sb = new StringBuilder();
        Type[] lowerBounds = wildcardType.getLowerBounds();
        Type[] bounds = lowerBounds;
        if (lowerBounds.length > 0) {
            sb.append("? super ");
        } else {
            Type[] upperBounds = wildcardType.getUpperBounds();
            if (upperBounds.length == 0 || upperBounds[0].equals(Object.class)) {
                return "?";
            }
            bounds = upperBounds;
            sb.append("? extends ");
        }

        boolean firstItem = true;
        for (Type type : bounds) {
            if (!firstItem) {
                sb.append(" & ");
            }

            firstItem = false;
            sb.append(getClassName(type, genericNames));
        }

        return sb.toString();
    }

    private static String getClassName(Type clazz) {
        return (clazz instanceof Class) ? ((Class<?>)clazz).getCanonicalName() : clazz.toString();
    }

    private static String getClassName(Type clazz, Map<String, String> genericNames) {
        if (clazz instanceof Class) {
            return ((Class<?>)clazz).getCanonicalName();
        } else if (clazz instanceof GenericArrayType) {
            return getClassName(((GenericArrayType) clazz).getGenericComponentType()) + "[]";
        } else if (clazz instanceof TypeVariable) {
            String name = ((TypeVariable<?>)clazz).getName();
            return genericNames.containsKey(name) ? genericNames.get(name) : name;
        } else if (clazz instanceof ParameterizedType) {
            ParameterizedType ptClazz = (ParameterizedType)clazz;
            return getClassName(ptClazz.getRawType()) + getTypeParameters(ptClazz.getActualTypeArguments(), genericNames);
        } else if (clazz instanceof WildcardType) {
            return getWildcardType((WildcardType)clazz, genericNames);
        }
        return clazz.toString();
    }

    private static String getParameterName(Type parameter, Map<String, String> genericNames) {
        if (parameter instanceof Class && ((Class<?>)parameter).isArray()) {
            return getParameterName(((Class<?>)parameter).getComponentType(), genericNames) + "[]";
        }
        return getClassName(parameter, genericNames);
    }

    private static String getSubclassName(Class<?> clazz) {
        return clazz.getSimpleName() + TESTABLE_IMPL;
    }

}
