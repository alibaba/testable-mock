package com.alibaba.testable.core.util;

import com.alibaba.testable.core.compile.InMemoryJavaCompiler;
import com.alibaba.testable.core.model.ConstructionOption;
import com.alibaba.testable.core.tool.OmniConstructor;

import java.lang.reflect.*;
import java.util.*;

import static com.alibaba.testable.core.constant.ConstPool.DOT;
import static com.alibaba.testable.core.model.ConstructionOption.RICH_INTERFACE;
import static com.alibaba.testable.core.util.CollectionUtil.entryOf;
import static com.alibaba.testable.core.util.CollectionUtil.mapOf;

public class ConstructionUtil {

    private static final String TESTABLE_IMPL = "$TestableImpl";

    private static final Map<String, String> RETURN_VALUES = mapOf(
            entryOf("java.lang.String", "\"mock\""),
            entryOf("byte", "'\0'"),
            entryOf("java.lang.Byte", "'\0'"),
            entryOf("char", "'\0'"),
            entryOf("java.lang.Character", "'\0'"),
            entryOf("double", "0.0D"),
            entryOf("java.lang.Double", "0.0D"),
            entryOf("float", "0.0"),
            entryOf("java.lang.Float", "0.0"),
            entryOf("int", "0"),
            entryOf("java.lang.Integer", "0"),
            entryOf("short", "0"),
            entryOf("java.lang.Short", "0"),
            entryOf("long", "0L"),
            entryOf("java.lang.Long", "0L"),
            entryOf("boolean", "true"),
            entryOf("java.lang.Boolean", "true")
    );

    public static <T> T generateSubClassOf(Class<T> clazz, ConstructionOption[] options) throws InstantiationException {
        StringBuilder sourceCode = new StringBuilder();
        String packageName = adaptName(clazz.getPackage().getName());
        Map<String, String> noMapping = new HashMap<String, String>();
        sourceCode.append("package ")
                .append(packageName)
                .append(";\npublic class ")
                .append(getSubclassName(clazz))
                .append(getTypeParameters(clazz.getTypeParameters(), true, noMapping))
                .append(clazz.isInterface() ? " implements " : " extends ")
                .append(getClassName(clazz, noMapping))
                .append(getTypeParameters(clazz.getTypeParameters(), false, noMapping))
                .append(" {\n");
        for (String method : generateMethodsOf(clazz, new HashSet<String>(), noMapping, options)) {
            sourceCode.append(method);
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

    private static Set<String> generateMethodsOf(Class<?> clazz, Set<String> methodPool,
                                                 Map<String, String> genericTypes, ConstructionOption[] options) {
        Set<String> methods = new HashSet<String>();
        // in a very special situation, getDeclaredMethods() could fetch method declaration in the parent interface
        // as none-abstract, that will cause the corresponding abstract method in current class be skipped.
        // it happens to e.g. CharSequence.subSequence(int,int) and CharBuffer.subSequence(int,int)
        // so we would pass the methods to pool after all method in current level have been handled.
        Set<String> thisLevelMethodPool = new HashSet<String>();
        for (Method m : clazz.getDeclaredMethods()) {
            StringBuilder methodSignatureBuilder = new StringBuilder(m.getName());
            for (Type p : m.getGenericParameterTypes()) {
                methodSignatureBuilder.append("#").append(getParameterName(p, genericTypes));
            }
            String methodSignature = methodSignatureBuilder.toString();
            if (methodPool.contains(methodSignature)) {
                continue;
            }
            thisLevelMethodPool.add(methodSignature);
            if (Modifier.isAbstract(m.getModifiers())) {
                StringBuilder sourceCode = new StringBuilder();
                sourceCode.append("\tpublic ")
                        .append(getTypeParameters(m.getTypeParameters(), true, genericTypes))
                        .append(getClassName(m.getGenericReturnType(), genericTypes))
                        .append(" ").append(m.getName()).append("(");
                Type[] parameters = m.getGenericParameterTypes();
                for (int i = 0; i < parameters.length; i++) {
                    sourceCode.append(getParameterName(parameters[i], genericTypes)).append(" p").append(i);
                    if (i < parameters.length - 1) {
                        sourceCode.append(", ");
                    }
                }
                sourceCode.append(") {\n");
                String returnType = getClassName(m.getGenericReturnType(), genericTypes);
                if (!"void".equals(returnType)) {
                    if (RETURN_VALUES.containsKey(returnType)) {
                        sourceCode.append("\t\treturn ").append(RETURN_VALUES.get(returnType)).append(";\n");
                    } else if (CollectionUtil.contains(options, RICH_INTERFACE)) {
                        sourceCode.append("\t\treturn (")
                                .append(returnType)
                                .append(") ")
                                .append(getClassName(OmniConstructor.class, genericTypes))
                                .append(".")
                                .append("newInstance(")
                                .append(getClassName(m.getReturnType(), genericTypes))
                                .append(".class);\n");
                    } else {
                        sourceCode.append("\t\treturn null;\n");
                    }
                }
                sourceCode.append("\t}\n");
                methods.add(sourceCode.toString());
            }
        }
        methodPool.addAll(thisLevelMethodPool);
        List<Type> superTypes = new ArrayList<Type>(Arrays.asList(clazz.getGenericInterfaces()));
        if (clazz.getGenericSuperclass() != null) {
            superTypes.add(clazz.getGenericSuperclass());
        }
        for (Type t : superTypes) {
            if (t instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) t;
                methods.addAll(generateMethodsOf((Class<?>) pt.getRawType(), methodPool, parseGenericTypes(pt), options));
            } else if (t instanceof Class) {
                methods.addAll(generateMethodsOf((Class<?>) t, methodPool, Collections.<String, String>emptyMap(), options));
            }
        }
        return methods;
    }

    private static Map<String, String> parseGenericTypes(ParameterizedType type) {
        Map<String, String> templateTypeMap = new HashMap<String, String>();
        Type[] actualTypeArguments = type.getActualTypeArguments();
        TypeVariable<? extends Class<?>>[] rawTypedParameters = ((Class<?>) type.getRawType()).getTypeParameters();
        for (int i = 0; i < actualTypeArguments.length; i++) {
            templateTypeMap.put(getClassName(rawTypedParameters[i]), getClassName(actualTypeArguments[i]));
        }
        return templateTypeMap;
    }

    private static String adaptName(String name) {
        // create class in 'java' package will cause 'prohibited package name' error
        return name.replaceAll("^java\\.", "testable.");
    }

    private static String getTypeParameters(TypeVariable<?>[] typeParameters, boolean withScope,
                                            Map<String, String> genericTypes) {
        if (typeParameters.length > 0) {
            StringBuilder sb = new StringBuilder("<");
            for (int i = 0; i < typeParameters.length; i++) {
                sb.append(typeParameters[i].getName());
                if (withScope) {
                    sb.append(" extends ");
                    Type[] bounds = typeParameters[i].getBounds();
                    for (int j = 0; j < bounds.length; j++) {
                        sb.append(getClassName(bounds[j], genericTypes));
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

    private static String getTypeParameters(Type[] typeParameters, Map<String, String> genericTypes) {
        if (typeParameters.length > 0) {
            StringBuilder sb = new StringBuilder("<");
            for (int i = 0; i < typeParameters.length; i++) {
                sb.append(getClassName(typeParameters[i], genericTypes));
                if (i < typeParameters.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("> ");
            return sb.toString();
        }
        return "";
    }

    private static String getWildcardType(WildcardType wildcardType, Map<String, String> genericTypes) {
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
            sb.append(getClassName(type, genericTypes));
        }

        return sb.toString();
    }

    private static String getClassName(Type clazz) {
        return (clazz instanceof Class) ? ((Class<?>)clazz).getCanonicalName() : clazz.toString();
    }

    private static String getClassName(Type clazz, Map<String, String> genericTypes) {
        if (clazz instanceof Class) {
            return ((Class<?>)clazz).getCanonicalName();
        } else if (clazz instanceof GenericArrayType) {
            return getClassName(((GenericArrayType) clazz).getGenericComponentType()) + "[]";
        } else if (clazz instanceof TypeVariable) {
            String name = ((TypeVariable<?>)clazz).getName();
            return genericTypes.containsKey(name) ? genericTypes.get(name) : name;
        } else if (clazz instanceof ParameterizedType) {
            ParameterizedType ptClazz = (ParameterizedType)clazz;
            return getClassName(ptClazz.getRawType()) + getTypeParameters(ptClazz.getActualTypeArguments(), genericTypes);
        } else if (clazz instanceof WildcardType) {
            return getWildcardType((WildcardType)clazz, genericTypes);
        }
        return clazz.toString();
    }

    private static String getParameterName(Type parameter, Map<String, String> genericTypes) {
        if (parameter instanceof Class && ((Class<?>)parameter).isArray()) {
            return getParameterName(((Class<?>)parameter).getComponentType(), genericTypes) + "[]";
        }
        return getClassName(parameter, genericTypes);
    }

    private static String getSubclassName(Class<?> clazz) {
        return clazz.getSimpleName() + TESTABLE_IMPL;
    }

}
