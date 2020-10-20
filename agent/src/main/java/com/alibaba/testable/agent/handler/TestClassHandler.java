package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.constant.ConstPool;
import com.alibaba.testable.agent.util.ClassUtil;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author flin
 */
public class TestClassHandler extends BaseClassHandler {

    private static final String CLASS_TESTABLE_TOOL = "com/alibaba/testable/core/tool/TestableTool";
    private static final String CLASS_TESTABLE_UTIL = "com/alibaba/testable/core/util/TestableUtil";
    private static final String FIELD_TEST_CASE = "TEST_CASE";
    private static final String FIELD_SOURCE_METHOD = "SOURCE_METHOD";
    private static final String METHOD_CURRENT_TEST_CASE_NAME = "currentTestCaseName";
    private static final String METHOD_CURRENT_SOURCE_METHOD_NAME = "currentSourceMethodName";
    private static final String SIGNATURE_TESTABLE_UTIL_METHOD = "(Ljava/lang/Object;)Ljava/lang/String;";
    private static final Map<String, String> FIELD_TO_METHOD_MAPPING = new HashMap<String, String>() {{
        put(FIELD_TEST_CASE, METHOD_CURRENT_TEST_CASE_NAME);
        put(FIELD_SOURCE_METHOD, METHOD_CURRENT_SOURCE_METHOD_NAME);
    }};

    /**
     * Handle bytecode of test class
     * @param cn original class node
     */
    @Override
    protected void transform(ClassNode cn) {
        for (MethodNode m : cn.methods) {
            transformMethod(cn, m);
        }
        cn.fields.add(new FieldNode(ACC_PUBLIC | ACC_STATIC, ConstPool.TESTABLE_INJECT_REF,
            ClassUtil.toByteCodeClassName(cn.name), null, null));
    }

    private void transformMethod(ClassNode cn, MethodNode mn) {
        handleAnnotation(cn, mn);
        handleInstruction(cn, mn);
    }

    private void handleAnnotation(ClassNode cn, MethodNode mn) {
        List<String> visibleAnnotationNames = new ArrayList<String>();
        if (mn.visibleAnnotations == null) {
            return;
        }
        for (AnnotationNode n : mn.visibleAnnotations) {
            visibleAnnotationNames.add(n.desc);
        }
        if (visibleAnnotationNames.contains(ClassUtil.toByteCodeClassName(ConstPool.TESTABLE_INJECT))) {
            mn.access &= ~ACC_PRIVATE;
            mn.access &= ~ACC_PROTECTED;
            mn.access |= ACC_PUBLIC;
        } else if (couldBeTestMethod(mn)) {
            injectTestableRef(cn, mn);
        }
    }

    private void handleInstruction(ClassNode cn, MethodNode mn) {
        AbstractInsnNode[] instructions = mn.instructions.toArray();
        int i = 0;
        do {
            if (instructions[i].getOpcode() == GETSTATIC) {
                FieldInsnNode fieldInsnNode = (FieldInsnNode)instructions[i];
                if (isTestableUtilField(fieldInsnNode)) {
                    instructions = replaceTestableUtilField(mn, instructions, fieldInsnNode.name, i);
                }
            }
            i++;
        } while (i < instructions.length);
    }

    private boolean isTestableUtilField(FieldInsnNode fieldInsnNode) {
        return fieldInsnNode.owner.equals(CLASS_TESTABLE_TOOL) &&
            (fieldInsnNode.name.equals(FIELD_TEST_CASE) || fieldInsnNode.name.equals(FIELD_SOURCE_METHOD));
    }

    private AbstractInsnNode[] replaceTestableUtilField(MethodNode mn, AbstractInsnNode[] instructions,
                                                        String fieldName, int pos) {
        InsnList insnNodes = new InsnList();
        insnNodes.insert(new VarInsnNode(ALOAD, 0));
        insnNodes.insert(new MethodInsnNode(INVOKESTATIC, CLASS_TESTABLE_UTIL, FIELD_TO_METHOD_MAPPING.get(fieldName),
            SIGNATURE_TESTABLE_UTIL_METHOD, false));
        mn.instructions.insertBefore(instructions[pos], insnNodes);
        mn.instructions.remove(instructions[pos]);
        return mn.instructions.toArray();
    }

    private void injectTestableRef(ClassNode cn, MethodNode mn) {
        InsnList il = new InsnList();
        il.add(new VarInsnNode(ALOAD, 0));
        il.add(new FieldInsnNode(PUTSTATIC, cn.name, ConstPool.TESTABLE_INJECT_REF,
            ClassUtil.toByteCodeClassName(cn.name)));
        mn.instructions.insertBefore(mn.instructions.get(0), il);
    }

    /**
     * Different unit test framework may have different @Test annotation
     * but they should always NOT private, protected or static
     */
    private boolean couldBeTestMethod(MethodNode mn) {
        return (mn.access & (ACC_PRIVATE | ACC_PROTECTED | ACC_STATIC)) == 0 ;
    }

}
