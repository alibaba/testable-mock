package com.alibaba.testable.agent.util;

import com.alibaba.testable.agent.constant.ConstPool;
import com.alibaba.testable.agent.tool.ImmutablePair;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.testable.agent.constant.ByteCodeConst.*;
import static com.alibaba.testable.core.constant.ConstPool.*;
import static org.objectweb.asm.Opcodes.*;

/**
 * @author flin
 */
public class ClassUtil {

    private static final String CLASS_BYTE = "java/lang/Byte";
    private static final String CLASS_CHARACTER = "java/lang/Character";
    private static final String CLASS_DOUBLE = "java/lang/Double";
    private static final String CLASS_FLOAT = "java/lang/Float";
    private static final String CLASS_INTEGER = "java/lang/Integer";
    private static final String CLASS_LONG = "java/lang/Long";
    private static final String CLASS_SHORT = "java/lang/Short";
    private static final String CLASS_BOOLEAN = "java/lang/Boolean";
    private static final String EMPTY = "";
    private static final String METHOD_VALUE_OF = "valueOf";
    private static final String METHOD_BYTE_VALUE = "byteValue";
    private static final String METHOD_CHAR_VALUE = "charValue";
    private static final String METHOD_DOUBLE_VALUE = "doubleValue";
    private static final String METHOD_FLOAT_VALUE = "floatValue";
    private static final String METHOD_INT_VALUE = "intValue";
    private static final String METHOD_LONG_VALUE = "longValue";
    private static final String METHOD_SHORT_VALUE = "shortValue";
    private static final String METHOD_BOOLEAN_VALUE = "booleanValue";

    private static final Map<Byte, String> TYPE_MAPPING = new HashMap<Byte, String>();
    private static final Map<Byte, ImmutablePair<String, String>> WRAPPER_METHOD_MAPPING =
        new HashMap<Byte, ImmutablePair<String, String>>();
    private static final Map<String, Integer> RETURN_OP_CODE_MAPPING = new HashMap<String, Integer>();

    static {
        TYPE_MAPPING.put(TYPE_BYTE, CLASS_BYTE);
        TYPE_MAPPING.put(TYPE_CHAR, CLASS_CHARACTER);
        TYPE_MAPPING.put(TYPE_DOUBLE, CLASS_DOUBLE);
        TYPE_MAPPING.put(TYPE_FLOAT, CLASS_FLOAT);
        TYPE_MAPPING.put(TYPE_INT, CLASS_INTEGER);
        TYPE_MAPPING.put(TYPE_LONG, CLASS_LONG);
        TYPE_MAPPING.put(TYPE_SHORT, CLASS_SHORT);
        TYPE_MAPPING.put(TYPE_BOOL, CLASS_BOOLEAN);
        TYPE_MAPPING.put(TYPE_VOID, EMPTY);
    }

    static {
        WRAPPER_METHOD_MAPPING.put(TYPE_BYTE, ImmutablePair.of(METHOD_BYTE_VALUE, "()" + (char)TYPE_BYTE));
        WRAPPER_METHOD_MAPPING.put(TYPE_CHAR, ImmutablePair.of(METHOD_CHAR_VALUE, "()" + (char)TYPE_CHAR));
        WRAPPER_METHOD_MAPPING.put(TYPE_DOUBLE, ImmutablePair.of(METHOD_DOUBLE_VALUE, "()" + (char)TYPE_DOUBLE));
        WRAPPER_METHOD_MAPPING.put(TYPE_FLOAT, ImmutablePair.of(METHOD_FLOAT_VALUE, "()" + (char)TYPE_FLOAT));
        WRAPPER_METHOD_MAPPING.put(TYPE_INT, ImmutablePair.of(METHOD_INT_VALUE, "()" + (char)TYPE_INT));
        WRAPPER_METHOD_MAPPING.put(TYPE_LONG, ImmutablePair.of(METHOD_LONG_VALUE, "()" + (char)TYPE_LONG));
        WRAPPER_METHOD_MAPPING.put(TYPE_SHORT, ImmutablePair.of(METHOD_SHORT_VALUE, "()" + (char)TYPE_SHORT));
        WRAPPER_METHOD_MAPPING.put(TYPE_BOOL, ImmutablePair.of(METHOD_BOOLEAN_VALUE, "()" + (char)TYPE_BOOL));
    }

    static {
        RETURN_OP_CODE_MAPPING.put(new String(new byte[] {TYPE_BYTE}), IRETURN);
        RETURN_OP_CODE_MAPPING.put(new String(new byte[] {TYPE_CHAR}), IRETURN);
        RETURN_OP_CODE_MAPPING.put(new String(new byte[] {TYPE_DOUBLE}), DRETURN);
        RETURN_OP_CODE_MAPPING.put(new String(new byte[] {TYPE_FLOAT}), FRETURN);
        RETURN_OP_CODE_MAPPING.put(new String(new byte[] {TYPE_INT}), IRETURN);
        RETURN_OP_CODE_MAPPING.put(new String(new byte[] {TYPE_LONG}), LRETURN);
        RETURN_OP_CODE_MAPPING.put(new String(new byte[] {TYPE_SHORT}), IRETURN);
        RETURN_OP_CODE_MAPPING.put(new String(new byte[] {TYPE_BOOL}), IRETURN);
    }

    /**
     * Fit kotlin companion class name to original name
     * @param name a class name (which could be a companion class)
     * @return is companion class or not
     */
    public static boolean isCompanionClassName(String name) {
        return name.endsWith(ConstPool.KOTLIN_POSTFIX_COMPANION);
    }

    /**
     * Fit kotlin companion class name to original name
     * @param name a class name (which could be a companion class)
     * @return original name
     */
    public static String fitCompanionClassName(String name) {
        return isCompanionClassName(name) ?
            name.substring(0, name.length() - ConstPool.KOTLIN_POSTFIX_COMPANION.length()) : name;
    }

    /**
     * Fit kotlin accessor method name to original name
     * @param name a accessor name (which could be a common kotlin method)
     * @return original name
     */
    public static String fitKotlinAccessorName(String name) {
        return name.startsWith(ConstPool.KOTLIN_PREFIX_ACCESS) ?
            name.substring(ConstPool.KOTLIN_PREFIX_ACCESS.length()) : name;
    }

    /**
     * Get mock class name from source class name
     * @param sourceClassName source class name
     * @return mock class name
     */
    public static String getMockClassName(String sourceClassName) {
        return sourceClassName + MOCK_POSTFIX;
    }

    /**
     * Get test class name from source class name
     * @param sourceClassName source class name
     * @return test class name
     */
    public static String getTestClassName(String sourceClassName) {
        return sourceClassName + TEST_POSTFIX;
    }

    /**
     * Get source class name from test class name
     * @param testClassName test class name
     * @return source class name
     */
    public static String getSourceClassName(String testClassName) {
        return testClassName.substring(0, testClassName.length() - TEST_POSTFIX.length());
    }

    /**
     * Get wrapper class of specified private type
     * @param primaryType byte code of private type
     * @return byte code of wrapper class
     */
    public static String toWrapperClass(Byte primaryType) {
        return TYPE_MAPPING.get(primaryType);
    }

    /**
     * Get method name and descriptor to convert wrapper type to primary type
     * @param primaryType byte code of private type
     * @return pair of [method-name, method-descriptor]
     */
    public static ImmutablePair<String, String> getWrapperTypeConvertMethod(byte primaryType) {
        return WRAPPER_METHOD_MAPPING.get(primaryType);
    }

    /**
     * Get byte code for return specified private type
     * @param type class type
     * @return byte code of return operation
     */
    public static int getReturnOpsCode(String type) {
        Integer code = RETURN_OP_CODE_MAPPING.get(type);
        return (code == null) ? ARETURN : code;
    }

    /**
     * Get method node to convert primary type to wrapper type
     * @param type primary type to convert
     * @return converter method node
     */
    public static MethodInsnNode getPrimaryTypeConvertMethod(Byte type) {
        String objectType = TYPE_MAPPING.get(type);
        return (objectType == null) ? null :
            new MethodInsnNode(INVOKESTATIC, objectType, METHOD_VALUE_OF, toDescriptor(type, objectType), false);
    }

    /**
     * Convert slash separated name to dot separated name
     * @param name original name
     * @return converted name
     */
    public static String toDotSeparatedName(String name) {
        return name.replace(SLASH, DOT);
    }

    /**
     * Convert dot separated name to slash separated name
     * @param name original name
     * @return converted name
     */
    public static String toSlashSeparatedName(String name) {
        return name.replace(DOT, SLASH);
    }

    /**
     * Convert dot separated name to byte code class name
     * @param className original name
     * @return converted name
     */
    public static String toByteCodeClassName(String className) {
        return (char)TYPE_CLASS + toSlashSeparatedName(className) + (char)CLASS_END;
    }

    /**
     * Convert byte code class name to slash separated human readable name
     * @param className original name
     * @return converted name
     */
    public static String toSlashSeparateJavaStyleName(String className) {
        return className.substring(1, className.length() - 1);
    }

    /**
     * Convert byte code class name to dot separated human readable name
     * @param className original name
     * @return converted name
     */
    public static String toJavaStyleClassName(String className) {
        return toDotSeparatedName(toSlashSeparateJavaStyleName(className));
    }

    /**
     * Read class from current context
     * @param className class name
     * @return loaded class
     */
    public static ClassNode getClassNode(String className) {
        ClassNode cn = new ClassNode();
        try {
            new ClassReader(className).accept(cn, 0);
        } catch (Throwable e) {
            // Could be IOException, ClassCircularityError or NullPointerException
            // Ignore all of them
            return null;
        }
        return cn;
    }

    /**
     * Get outer class name from a inner class name
     * @param name inner class name
     * @return outer class name
     */
    public static String toOuterClassName(String name) {
        int pos = name.lastIndexOf("$");
        return (pos > 0) ? name.substring(0, pos) : name;
    }

    private static String toDescriptor(Byte type, String objectType) {
        return "(" + (char)type.byteValue() + ")L" + objectType + ";";
    }
}
