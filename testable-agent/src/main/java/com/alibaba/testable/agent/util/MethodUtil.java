package com.alibaba.testable.agent.util;

import com.alibaba.testable.agent.tool.ImmutablePair;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

import static com.alibaba.testable.agent.constant.ByteCodeConst.*;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

public class MethodUtil {

    private static final String COMMA_SPACE = ", ";
    private static final int MINIMAL_DESC_LENGTH_OF_TWO_PARAMETERS_METHOD = 4;

    /**
     * Judge whether a method is static
     * @param mn method to check
     * @return is static or not
     */
    public static boolean isStatic(MethodNode mn) {
        return (mn.access & ACC_STATIC) != 0;
    }

    /**
     * Parse method desc, fetch parameter types
     * @param desc method description
     * @return list of parameter types
     */
    public static List<Byte> getParameterTypes(String desc) {
        List<Byte> parameterTypes = new ArrayList<Byte>();
        boolean travelingClass = false;
        boolean travelingArray = false;
        for (byte b : desc.getBytes()) {
            if (travelingClass) {
                if (b == CLASS_END) {
                    travelingClass = false;
                    travelingArray = false;
                }
            } else {
                if (isPrimaryType(b)) {
                    // should treat primary array as class (issue-48)
                    parameterTypes.add(travelingArray ? TYPE_CLASS : b);
                    travelingArray = false;
                } else if (b == TYPE_CLASS) {
                    travelingClass = true;
                    parameterTypes.add(b);
                } else if (b == TYPE_ARRAY) {
                    travelingArray = true;
                } else if (b == PARAM_END) {
                    break;
                }
            }
        }
        return parameterTypes;
    }

    /**
     * Extract parameter part of method desc
     * @param desc method description
     * @return parameter value
     */
    public static String extractParameters(String desc) {
        int returnTypeEdge = desc.lastIndexOf(PARAM_END);
        return desc.substring(1, returnTypeEdge);
    }

    /**
     * Parse method desc, fetch return value type
     * @param desc method description
     * @return types of return value
     */
    public static String getReturnType(String desc) {
        int returnTypeEdge = desc.lastIndexOf(PARAM_END);
        return desc.substring(returnTypeEdge + 1);
    }

    /**
     * Parse method desc, fetch parameter types string
     * @param desc method description
     * @return parameter types
     */
    public static Object getParameters(String desc) {
        int returnTypeEdge = desc.lastIndexOf(PARAM_END);
        return desc.substring(1, returnTypeEdge);
    }

    /**
     * Parse method desc, fetch first parameter type (assume first parameter is an object type)
     * @param desc method description
     * @return types of first parameter
     */
    public static String getFirstParameter(String desc) {
        // assume first parameter is class type
        return desc.substring(1, desc.indexOf(CLASS_END) + 1);
    }

    /**
     * Split desc to "first parameter" and "desc of rest parameters"
     * @param desc method desc
     * @return pair of [slash separated first parameter type, desc of rest parameters]
     */
    public static ImmutablePair<String, String> splitFirstAndRestParameters(String desc) {
        if (desc.length() < MINIMAL_DESC_LENGTH_OF_TWO_PARAMETERS_METHOD) {
            return ImmutablePair.of("", "");
        }
        if (desc.charAt(1) != TYPE_CLASS) {
            return ImmutablePair.of(desc.substring(1, 2), "(" + desc.substring(2));
        }
        int pos = desc.indexOf(";");
        return pos < 0 ? ImmutablePair.of("", "")
            : ImmutablePair.of(desc.substring(2, pos), "(" + desc.substring(pos + 1));
    }

    /**
     * Remove first parameter from method descriptor
     * @param desc original descriptor
     * @return descriptor without first parameter
     */
    public static String removeFirstParameter(String desc) {
        return "(" + desc.substring(desc.indexOf(";") + 1);
    }

    /**
     * Add extra parameter to the beginning of method descriptor
     * @param desc original descriptor
     * @param type byte code class name
     * @return descriptor with specified parameter at begin
     */
    public static String addParameterAtBegin(String desc, String type) {
        return "(" + type + desc.substring(1);
    }

    private static boolean isPrimaryType(byte b) {
        return b == TYPE_BYTE || b == TYPE_CHAR || b == TYPE_DOUBLE || b == TYPE_FLOAT
            || b == TYPE_INT || b == TYPE_LONG || b == TYPE_SHORT || b == TYPE_BOOL;
    }

    /**
     * Format to java style constructor descriptor
     * @param owner class of method belongs to
     * @param desc method constructor in bytecode format
     * @return java style constructor descriptor
     */
    public static String toJavaMethodDesc(String owner, String desc) {
        String ownerInDotFormat = ClassUtil.toDotSeparatedName(owner);
        String parameters = toJavaParameterDesc(extractParameters(desc));
        return String.format("%s(%s)", ownerInDotFormat, parameters);
    }

    /**
     * Format to java style method descriptor
     * @param owner class of method belongs to
     * @param name method name
     * @param desc method descriptor in bytecode format
     * @return java style method descriptor
     */
    public static String toJavaMethodDesc(String owner, String name, String desc) {
        String ownerInDotFormat = ClassUtil.toDotSeparatedName(owner);
        String returnType = toJavaParameterDesc(getReturnType(desc));
        String parameters = toJavaParameterDesc(extractParameters(desc));
        return String.format("%s::%s(%s) : %s", ownerInDotFormat, name, parameters, returnType);
    }

    /**
     * Convert bytecode style parameter descriptor to java style descriptor
     * @param desc bytecode style descriptor
     * @return java style descriptor
     */
    private static String toJavaParameterDesc(String desc) {
        if (desc.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean isArray = false;
        boolean isTravellingClass = false;
        for (byte b : desc.getBytes()) {
            if (isTravellingClass) {
                switch (b) {
                    case CLASS_END:
                        sb.append(isArray ? "[]" : "");
                        isArray = false;
                        isTravellingClass = false;
                        break;
                    case PKG_SEGMENT:
                        sb.append('.');
                        break;
                    default:
                        sb.append((char)b);
                }
            } else {
                switch (b) {
                    case TYPE_ARRAY:
                        isArray = true;
                        break;
                    case TYPE_VOID:
                        sb.append(COMMA_SPACE).append("void");
                        break;
                    case TYPE_BYTE:
                        sb.append(COMMA_SPACE).append("byte").append(isArray ? "[]" : "");
                        isArray = false;
                        break;
                    case TYPE_CHAR:
                        sb.append(COMMA_SPACE).append("char").append(isArray ? "[]" : "");
                        isArray = false;
                        break;
                    case TYPE_DOUBLE:
                        sb.append(COMMA_SPACE).append("double").append(isArray ? "[]" : "");
                        isArray = false;
                        break;
                    case TYPE_FLOAT:
                        sb.append(COMMA_SPACE).append("float").append(isArray ? "[]" : "");
                        isArray = false;
                        break;
                    case TYPE_INT:
                        sb.append(COMMA_SPACE).append("int").append(isArray ? "[]" : "");
                        isArray = false;
                        break;
                    case TYPE_LONG:
                        sb.append(COMMA_SPACE).append("long").append(isArray ? "[]" : "");
                        isArray = false;
                        break;
                    case TYPE_SHORT:
                        sb.append(COMMA_SPACE).append("short").append(isArray ? "[]" : "");
                        isArray = false;
                        break;
                    case TYPE_BOOL:
                        sb.append(COMMA_SPACE).append("boolean").append(isArray ? "[]" : "");
                        isArray = false;
                        break;
                    case TYPE_CLASS:
                        sb.append(COMMA_SPACE);
                        isTravellingClass = true;
                        break;
                    default:
                        break;
                }
            }
        }
        return sb.substring(COMMA_SPACE.length());
    }
}
