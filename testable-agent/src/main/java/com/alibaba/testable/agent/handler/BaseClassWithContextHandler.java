package com.alibaba.testable.agent.handler;

import org.objectweb.asm.tree.*;

/**
 * @author flin
 */
abstract public class BaseClassWithContextHandler extends BaseClassHandler {

    protected static final String CLASS_MOCK_CONTEXT_UTIL = "com/alibaba/testable/core/util/MockContextUtil";
    private static final String CLASS_TESTABLE_TOOL = "com/alibaba/testable/core/tool/TestableTool";
    private static final String CLASS_TESTABLE_UTIL = "com/alibaba/testable/core/util/TestableUtil";
    private static final String FIELD_SOURCE_METHOD = "SOURCE_METHOD";
    private static final String FIELD_MOCK_CONTEXT = "MOCK_CONTEXT";
    private static final String METHOD_PARAMETERS = "parameters";
    private static final String METHOD_CURRENT_SOURCE_METHOD_NAME = "currentSourceMethodName";
    private static final String SIGNATURE_CURRENT_SOURCE_METHOD_NAME = "()Ljava/lang/String;";
    private static final String SIGNATURE_PARAMETERS = "()Ljava/util/Map;";

    protected void handleTestableUtil(MethodNode mn) {
        AbstractInsnNode[] instructions = mn.instructions.toArray();
        // Note: instructions.length will change when instructions updated
        for (int i = 0; i < instructions.length; i++) {
            if (instructions[i].getOpcode() == GETSTATIC) {
                FieldInsnNode fieldInsnNode = (FieldInsnNode)instructions[i];
                if (isTestableUtilField(fieldInsnNode)) {
                    replaceTestableUtilField(mn, instructions, fieldInsnNode.name, i);
                    instructions = mn.instructions.toArray();
                }
            }
        }
    }

    private boolean isTestableUtilField(FieldInsnNode fieldInsnNode) {
        return fieldInsnNode.owner.equals(CLASS_TESTABLE_TOOL) &&
            (fieldInsnNode.name.equals(FIELD_SOURCE_METHOD) || fieldInsnNode.name.equals(FIELD_MOCK_CONTEXT));
    }

    private void replaceTestableUtilField(MethodNode mn, AbstractInsnNode[] instructions, String fieldName, int pos) {
        InsnList il = new InsnList();
        if (FIELD_SOURCE_METHOD.equals(fieldName)) {
            il.add(new MethodInsnNode(INVOKESTATIC, CLASS_TESTABLE_UTIL, METHOD_CURRENT_SOURCE_METHOD_NAME,
                SIGNATURE_CURRENT_SOURCE_METHOD_NAME, false));
        } else if (FIELD_MOCK_CONTEXT.equals(fieldName)) {
            il.add(new MethodInsnNode(INVOKESTATIC, CLASS_MOCK_CONTEXT_UTIL, METHOD_PARAMETERS,
                SIGNATURE_PARAMETERS, false));
        }
        if (il.size() > 0) {
            mn.instructions.insert(instructions[pos], il);
            mn.instructions.remove(instructions[pos]);
        }
    }

}
