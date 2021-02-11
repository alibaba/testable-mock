package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.util.ClassUtil;
import org.objectweb.asm.tree.*;

/**
 * @author flin
 */
abstract public class BaseClassWithContextHandler extends BaseClassHandler {

    private static final String CLASS_TESTABLE_TOOL = "com/alibaba/testable/core/tool/TestableTool";
    private static final String CLASS_TESTABLE_UTIL = "com/alibaba/testable/core/util/TestableUtil";
    private static final String CLASS_MOCK_CONTEXT = "com/alibaba/testable/agent/model/MockContext";
    private static final String FIELD_TEST_CASE = "TEST_CASE";
    private static final String FIELD_SOURCE_METHOD = "SOURCE_METHOD";
    private static final String FIELD_MOCK_CONTEXT = "MOCK_CONTEXT";
    private static final String FIELD_PARAMETERS = "parameters";
    private static final String METHOD_CURRENT_TEST_CASE_NAME = "currentTestCaseName";
    private static final String METHOD_CURRENT_SOURCE_METHOD_NAME = "currentSourceMethodName";
    private static final String METHOD_GET_TEST_CASE_MARK = "getTestCaseMark";
    private static final String SIGNATURE_CURRENT_TEST_CASE_NAME = "(Ljava/lang/String;)Ljava/lang/String;";
    private static final String SIGNATURE_CURRENT_SOURCE_METHOD_NAME = "()Ljava/lang/String;";
    private static final String SIGNATURE_GET_TEST_CASE_MARK = "()Ljava/lang/String;";
    private static final String SIGNATURE_PARAMETERS = "Ljava/util/Map;";
    private static final String CLASS_MAP = "java/util/Map";
    private static final String METHOD_MAP_GET = "get";
    private static final String SIGNATURE_MAP_GET = "(Ljava/lang/Object;)Ljava/lang/Object;";

    protected AbstractInsnNode[] handleTestableUtil(ClassNode cn, MethodNode mn, AbstractInsnNode[] instructions, int i) {
        if (instructions[i].getOpcode() == GETSTATIC) {
            FieldInsnNode fieldInsnNode = (FieldInsnNode)instructions[i];
            if (isTestableUtilField(fieldInsnNode)) {
                instructions = replaceTestableUtilField(cn, mn, instructions, fieldInsnNode.name, i);
            }
        }
        return instructions;
    }

    private boolean isTestableUtilField(FieldInsnNode fieldInsnNode) {
        return fieldInsnNode.owner.equals(CLASS_TESTABLE_TOOL) &&
            (fieldInsnNode.name.equals(FIELD_TEST_CASE) || fieldInsnNode.name.equals(FIELD_SOURCE_METHOD) ||
                fieldInsnNode.name.equals(FIELD_MOCK_CONTEXT));
    }

    private AbstractInsnNode[] replaceTestableUtilField(ClassNode cn, MethodNode mn, AbstractInsnNode[] instructions,
                                                        String fieldName, int pos) {
        InsnList il = new InsnList();
        if (FIELD_TEST_CASE.equals(fieldName)) {
            il.add(new LdcInsnNode(ClassUtil.toDotSeparatedName(cn.name)));
            il.add(new MethodInsnNode(INVOKESTATIC, CLASS_TESTABLE_UTIL, METHOD_CURRENT_TEST_CASE_NAME,
                SIGNATURE_CURRENT_TEST_CASE_NAME, false));
        } else if (FIELD_SOURCE_METHOD.equals(fieldName)) {
            il.add(new MethodInsnNode(INVOKESTATIC, CLASS_TESTABLE_UTIL, METHOD_CURRENT_SOURCE_METHOD_NAME,
                SIGNATURE_CURRENT_SOURCE_METHOD_NAME, false));
        } else if (FIELD_MOCK_CONTEXT.equals(fieldName)) {
            il.add(new FieldInsnNode(GETSTATIC, CLASS_MOCK_CONTEXT, FIELD_PARAMETERS, SIGNATURE_PARAMETERS));
            il.add(new VarInsnNode(ALOAD, 0));
            il.add(new MethodInsnNode(INVOKESPECIAL, cn.name, METHOD_GET_TEST_CASE_MARK,
                SIGNATURE_GET_TEST_CASE_MARK, false));
            il.add(new MethodInsnNode(INVOKEINTERFACE, CLASS_MAP, METHOD_MAP_GET,
                SIGNATURE_MAP_GET, true));
        }
        if (il.size() > 0) {
            mn.instructions.insert(instructions[pos], il);
            mn.instructions.remove(instructions[pos]);
        }
        return mn.instructions.toArray();
    }

    /**
     * get mark compose by TestClass and TestCase
     * @return test case mark
     */
    abstract protected String getTestCaseMark();

}
