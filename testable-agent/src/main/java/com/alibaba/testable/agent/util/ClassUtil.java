package com.alibaba.testable.agent.util;

import com.alibaba.testable.agent.constant.ConstPool;
import com.alibaba.testable.agent.tool.ComparableWeakRef;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;

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
    private static final Map<ComparableWeakRef<String>, Boolean> loadedClass =
        new WeakHashMap<ComparableWeakRef<String>, Boolean>();

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
     * Check whether any method in specified class has specified annotation
     * @param className class that need to explore
     * @param annotationName annotation to look for
     */
    public static boolean anyMethodHasAnnotation(String className, String annotationName) {
        Boolean found = loadedClass.get(new ComparableWeakRef<String>(className));
        if (found != null) {
            return found;
        }
        try {
            ClassNode cn = new ClassNode();
            new ClassReader(className).accept(cn, 0);
            for (MethodNode mn : cn.methods) {
                if (mn.visibleAnnotations != null) {
                    for (AnnotationNode an : mn.visibleAnnotations) {
                        if (toDotSeparateFullClassName(an.desc).equals(annotationName)) {
                            loadedClass.put(new ComparableWeakRef<String>(className), true);
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
        loadedClass.put(new ComparableWeakRef<String>(className), false);
        return false;
    }

    /**
     * get test class name from source class name
     * @param sourceClassName source class name
     */
    public static String getTestClassName(String sourceClassName) {
        return sourceClassName + ConstPool.TEST_POSTFIX;
    }

    /**
     * get source class name from test class name
     * @param testClassName test class name
     */
    public static String getSourceClassName(String testClassName) {
        return testClassName.substring(0, testClassName.length() - ConstPool.TEST_POSTFIX.length());
    }

    /**
     * parse method desc, fetch parameter types
     */
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

    /**
     * parse method desc, fetch return value types
     */
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

    /**
     * convert slash separated name to dot separated name
     */
    public static String toDotSeparatedName(String name) {
        return name.replace(ConstPool.SLASH, ConstPool.DOT);
    }

    /**
     * convert dot separated name to slash separated name
     */
    public static String toSlashSeparatedName(String name) {
        return name.replace(ConstPool.DOT, ConstPool.SLASH);
    }

    /**
     * convert dot separated name to byte code class name
     */
    public static String toByteCodeClassName(String className) {
        return TYPE_CLASS + toSlashSeparatedName(className) + CLASS_END;
    }

    /**
     * convert byte code class name to dot separated human readable name
     */
    public static String toDotSeparateFullClassName(String className) {
        return toDotSeparatedName(className).substring(1, className.length() - 1);
    }

    /**
     * convert byte code class name to slash separated human readable name
     */
    public static String toSlashSeparateFullClassName(String className) {
        return toSlashSeparatedName(className).substring(1, className.length() - 1);
    }

    private static boolean isPrimaryType(byte b) {
        return b == TYPE_BYTE || b == TYPE_CHAR || b == TYPE_DOUBLE || b == TYPE_FLOAT
            || b == TYPE_INT || b == TYPE_LONG || b == TYPE_SHORT || b == TYPE_BOOL;
    }

}
