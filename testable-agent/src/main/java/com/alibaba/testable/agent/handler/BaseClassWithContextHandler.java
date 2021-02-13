package com.alibaba.testable.agent.handler;

import com.alibaba.testable.core.util.MockContextUtil;
import org.objectweb.asm.tree.*;

/**
 * @author flin
 */
abstract public class BaseClassWithContextHandler extends BaseClassHandler {

    private static final String CLASS_TESTABLE_TOOL = "com/alibaba/testable/core/tool/TestableTool";
    private static final String CLASS_TESTABLE_UTIL = "com/alibaba/testable/core/util/TestableUtil";
    private static final String CLASS_MOCK_CONTEXT_HOLDER = "com/alibaba/testable/agent/model/MockContextHolder";
    private static final String FIELD_SOURCE_METHOD = "SOURCE_METHOD";
    private static final String FIELD_MOCK_CONTEXT = "MOCK_CONTEXT";
    private static final String FIELD_PARAMETERS = "parameters";
    private static final String METHOD_CURRENT_SOURCE_METHOD_NAME = "currentSourceMethodName";
    private static final String METHOD_GET_TEST_CASE_MARK = "getTestCaseMark";
    private static final String SIGNATURE_CURRENT_SOURCE_METHOD_NAME = "()Ljava/lang/String;";
    private static final String SIGNATURE_GET_TEST_CASE_MARK = "()Ljava/lang/String;";
    private static final String SIGNATURE_PARAMETERS = "Ljava/util/Map;";
    private static final String CLASS_MAP = "java/util/Map";
    private static final String METHOD_MAP_GET = "get";
    private static final String SIGNATURE_MAP_GET = "(Ljava/lang/Object;)Ljava/lang/Object;";
    private static final String CLASS_BASE_CLASS_WITH_CONTEXT_HANDLER
        = "com/alibaba/testable/agent/handler/BaseClassWithContextHandler";

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
            (fieldInsnNode.name.equals(FIELD_SOURCE_METHOD) || fieldInsnNode.name.equals(FIELD_MOCK_CONTEXT));
    }

    private AbstractInsnNode[] replaceTestableUtilField(ClassNode cn, MethodNode mn, AbstractInsnNode[] instructions,
                                                        String fieldName, int pos) {
        InsnList il = new InsnList();
        if (FIELD_SOURCE_METHOD.equals(fieldName)) {
            il.add(new MethodInsnNode(INVOKESTATIC, CLASS_TESTABLE_UTIL, METHOD_CURRENT_SOURCE_METHOD_NAME,
                SIGNATURE_CURRENT_SOURCE_METHOD_NAME, false));
        } else if (FIELD_MOCK_CONTEXT.equals(fieldName)) {
            il.add(new FieldInsnNode(GETSTATIC, CLASS_MOCK_CONTEXT_HOLDER, FIELD_PARAMETERS, SIGNATURE_PARAMETERS));
            il.add(new MethodInsnNode(INVOKESTATIC, CLASS_BASE_CLASS_WITH_CONTEXT_HANDLER, METHOD_GET_TEST_CASE_MARK,
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

    public static String getTestCaseMark() {
        String testClass = MockContextUtil.context.get().testClassName;
        String testCaseName = MockContextUtil.context.get().testCaseName;
        return testClass + "::" + testCaseName;
    }

}
