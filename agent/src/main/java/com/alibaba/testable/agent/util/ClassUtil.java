package com.alibaba.testable.agent.util;

import com.alibaba.testable.agent.constant.ConstPool;
import com.alibaba.testable.agent.model.MethodInfo;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author flin
 */
public class ClassUtil {

    private static final char TYPE_BYTE = 'B';
    private static final char TYPE_CHAR = 'C';
    private static final char TYPE_DOUBLE = 'D';
    private static final char TYPE_FLOAT = 'F';
    private static final char TYPE_INT = 'I';
    private static final char TYPE_LONG = 'J';
    private static final char TYPE_CLASS = 'L';
    private static final char TYPE_SHORT = 'S';
    private static final char TYPE_BOOL = 'Z';
    private static final char PARAM_END = ')';
    private static final char CLASS_END = ';';
    private static final char TYPE_ARRAY = '[';

    private static final Map<Character, String> TYPE_MAPPING = new HashMap<Character, String>();
    private static final String TARGET_CLASS = "targetClass";
    private static final String TARGET_METHOD = "targetMethod";

    static {
        TYPE_MAPPING.put(TYPE_BYTE, "java/lang/Byte");
        TYPE_MAPPING.put(TYPE_CHAR, "java/lang/Character");
        TYPE_MAPPING.put(TYPE_DOUBLE, "java/lang/Double");
        TYPE_MAPPING.put(TYPE_FLOAT, "java/lang/Float");
        TYPE_MAPPING.put(TYPE_INT, "java/lang/Integer");
        TYPE_MAPPING.put(TYPE_LONG, "java/lang/Long");
        TYPE_MAPPING.put(TYPE_SHORT, "java/lang/Short");
        TYPE_MAPPING.put(TYPE_BOOL, "java/lang/Boolean");
    }

    /**
     * Get annotation on class definition
     * @param className class that need to explore
     */
    public static List<String> getAnnotations(String className) {
        try {
            List<String> annotations = new ArrayList<String>();
            ClassNode cn = new ClassNode();
            new ClassReader(className).accept(cn, 0);
            for (AnnotationNode an : cn.visibleAnnotations) {
                annotations.add(toDotSeparateFullClassName(an.desc));
            }
            return annotations;
        } catch (Exception e) {
            return new ArrayList<String>();
        }
    }

    /**
     * Get testable inject method from test class
     * @param className test class name
     */
    public static List<MethodInfo> getTestableInjectMethods(String className) {
        try {
            List<MethodInfo> methodInfos = new ArrayList<MethodInfo>();
            ClassNode cn = new ClassNode();
            new ClassReader(className).accept(cn, 0);
            for (MethodNode mn : cn.methods) {
                checkMethodAnnotation(cn, methodInfos, mn);
            }
            return methodInfos;
        } catch (Exception e) {
            return new ArrayList<MethodInfo>();
        }
    }

    private static void checkMethodAnnotation(ClassNode cn, List<MethodInfo> methodInfos, MethodNode mn) {
        if (mn.visibleAnnotations == null) {
            return;
        }
        for (AnnotationNode an : mn.visibleAnnotations) {
            if (toDotSeparateFullClassName(an.desc).equals(ConstPool.TESTABLE_INJECT)) {
                String targetClass = getAnnotationParameter(an, TARGET_CLASS, StringUtil.getSourceClassName(cn.name));
                String targetMethod = getAnnotationParameter(an, TARGET_METHOD, mn.name);
                methodInfos.add(new MethodInfo(toSlashSeparateName(targetClass), targetMethod, mn.desc));
                break;
            }
        }
    }

    private static String getAnnotationParameter(AnnotationNode an, String key, String defaultValue) {
        if (an.values != null) {
            int i = an.values.indexOf(key);
            if (i % 2 == 0) {
                return (String)an.values.get(i+1);
            }
        }
        return defaultValue;
    }

    public static List<Byte> getParameterTypes(String desc) {
        List<Byte> parameterTypes = new ArrayList<Byte>();
        boolean travelingClass = false;
        for (byte b : desc.getBytes()) {
            if (travelingClass) {
                if (b == CLASS_END) {
                    travelingClass = false;
                }
            } else {
                if (isPrimaryType(b)) {
                    parameterTypes.add(b);
                } else if (b == TYPE_CLASS) {
                    travelingClass = true;
                    parameterTypes.add(b);
                } else if (b == PARAM_END) {
                    break;
                }
            }
        }
        return parameterTypes;
    }

    public static String getReturnType(String desc) {
        int returnTypeEdge = desc.lastIndexOf(PARAM_END);
        char typeChar = desc.charAt(returnTypeEdge + 1);
        if (typeChar == TYPE_ARRAY) {
            return desc.substring(returnTypeEdge + 1);
        } else if (typeChar == TYPE_CLASS) {
            return desc.substring(returnTypeEdge + 2, desc.length() - 1);
        } else if (TYPE_MAPPING.containsKey(typeChar)) {
            return TYPE_MAPPING.get(typeChar);
        } else {
            return "";
        }
    }

    private static String toSlashSeparateName(String name) {
        return name.replace(ConstPool.DOT, ConstPool.SLASH);
    }

    public static String toByteCodeClassName(String className) {
        return TYPE_CLASS + toSlashSeparateName(className) + CLASS_END;
    }

    public static String toDotSeparateFullClassName(String className) {
        return className.replace(ConstPool.SLASH, ConstPool.DOT).substring(1, className.length() - 1);
    }

    private static boolean isPrimaryType(byte b) {
        return b == TYPE_BYTE || b == TYPE_CHAR || b == TYPE_DOUBLE || b == TYPE_FLOAT
            || b == TYPE_INT || b == TYPE_LONG || b == TYPE_SHORT || b == TYPE_BOOL;
    }

}
