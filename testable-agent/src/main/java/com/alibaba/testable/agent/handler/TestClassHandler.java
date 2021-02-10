package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.constant.ConstPool;
import com.alibaba.testable.agent.tool.ImmutablePair;
import com.alibaba.testable.agent.util.AnnotationUtil;
import com.alibaba.testable.agent.util.ClassUtil;
import com.alibaba.testable.core.util.LogUtil;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import javax.lang.model.type.NullType;
import java.util.List;

import static com.alibaba.testable.agent.constant.ConstPool.CONSTRUCTOR;
import static com.alibaba.testable.agent.util.ClassUtil.toDotSeparateFullClassName;

/**
 * @author flin
 */
public class TestClassHandler extends BaseClassHandler {

    private static final String CLASS_TESTABLE_TOOL = "com/alibaba/testable/core/tool/TestableTool";
    private static final String CLASS_TESTABLE_UTIL = "com/alibaba/testable/core/util/TestableUtil";
    private static final String CLASS_MOCK_CONTEXT = "com/alibaba/testable/agent/model/MockContext";
    private static final String REF_TESTABLE_CONTEXT = "_testableContextReference";
    private static final String FIELD_TEST_CASE = "TEST_CASE";
    private static final String FIELD_SOURCE_METHOD = "SOURCE_METHOD";
    private static final String FIELD_MOCK_CONTEXT = "MOCK_CONTEXT";
    private static final String FIELD_PARAMETERS = "parameters";
    private static final String METHOD_CURRENT_TEST_CASE_NAME = "currentTestCaseName";
    private static final String METHOD_CURRENT_SOURCE_METHOD_NAME = "currentSourceMethodName";
    private static final String SIGNATURE_CURRENT_TEST_CASE_NAME = "(Ljava/lang/String;)Ljava/lang/String;";
    private static final String SIGNATURE_CURRENT_SOURCE_METHOD_NAME = "()Ljava/lang/String;";
    private static final String SIGNATURE_PARAMETERS = "Ljava/util/Map;";

    public TestClassHandler(String mockClassName) {
        this.mockClassName = mockClassName;
    }

    /**
     * Handle bytecode of test class
     * @param cn original class node
     */
    @Override
    protected void transform(ClassNode cn) {
        if (wasTransformed(cn, REF_TESTABLE_CONTEXT, ClassUtil.toByteCodeClassName(mockClassName))) {
            return;
        }
        for (MethodNode mn : cn.methods) {
            if (mn.name.equals(ConstPool.CONSTRUCTOR)) {
                initMockContextReference(cn, mn);
            } else {
                handleInstruction(cn, mn);
            }
        }
    }

    private void initMockContextReference(ClassNode cn, MethodNode mn) {
        InsnList il = new InsnList();
        il.add(new TypeInsnNode(NEW, CLASS_MOCK_CONTEXT));
        il.add(new InsnNode(DUP));
        il.add(new MethodInsnNode(INVOKESPECIAL, CLASS_MOCK_CONTEXT, CONSTRUCTOR, "()V", false));
        il.add(new FieldInsnNode(PUTSTATIC, cn.name, REF_TESTABLE_CONTEXT,
            ClassUtil.toByteCodeClassName(CLASS_MOCK_CONTEXT)));
        mn.instructions.insertBefore(mn.instructions.get(0), il);
        mn.maxStack++;
    }

    private void handleInstruction(ClassNode cn, MethodNode mn) {
        AbstractInsnNode[] instructions = mn.instructions.toArray();
        int i = 0;
        do {
            if (instructions[i].getOpcode() == GETSTATIC) {
                FieldInsnNode fieldInsnNode = (FieldInsnNode)instructions[i];
                if (isTestableUtilField(fieldInsnNode)) {
                    instructions = replaceTestableUtilField(cn, mn, instructions, fieldInsnNode.name, i);
                }
            }
            i++;
        } while (i < instructions.length);
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
            il.add(new FieldInsnNode(GETSTATIC, cn.name, REF_TESTABLE_CONTEXT,
                ClassUtil.toByteCodeClassName(CLASS_MOCK_CONTEXT)));
            il.add(new FieldInsnNode(GETFIELD, CLASS_MOCK_CONTEXT, FIELD_PARAMETERS, SIGNATURE_PARAMETERS));
        }
        if (il.size() > 0) {
            mn.instructions.insert(instructions[pos], il);
            mn.instructions.remove(instructions[pos]);
        }
        return mn.instructions.toArray();
    }

}
