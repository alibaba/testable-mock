package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.handler.test.JUnit4Framework;
import com.alibaba.testable.agent.handler.test.JUnit5Framework;
import com.alibaba.testable.agent.util.ClassUtil;
import com.alibaba.testable.agent.util.CollectionUtil;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

import java.util.List;

import static com.alibaba.testable.agent.util.ClassUtil.CLASS_OBJECT;
import static com.alibaba.testable.core.constant.ConstPool.CONSTRUCTOR;
import static com.alibaba.testable.core.constant.ConstPool.THIS_REF;

/**
 * @author flin
 */
public class OmniClassHandler extends BaseClassHandler {

    private static final String TESTABLE_NULL_TYPE = "com.alibaba.testable.core.model.TestableNull";
    private static final String NULL_TYPE = "javax.lang.model.type.NullType";
    private static final String IGNORE = "ignore";
    private static final String METHOD_START = "(";
    private static final String VOID_METHOD_END = ")V";
    private static final String VOID_METHOD = "()V";

    private static final String[] JUNIT_TEST_ANNOTATIONS = new String[] {
        JUnit4Framework.ANNOTATION_TEST, JUnit5Framework.ANNOTATION_TEST, JUnit5Framework.ANNOTATION_PARAMETERIZED_TEST
    };

    @Override
    protected void transform(ClassNode cn) {
        if (isInterface(cn) || isJunitTestClass(cn) || isUninstantiableClass(cn)) {
            return;
        }
        MethodNode constructor = new MethodNode(ACC_PUBLIC, CONSTRUCTOR,
            METHOD_START + ClassUtil.toByteCodeClassName(NULL_TYPE) + VOID_METHOD_END, null, null);
        LabelNode start = new LabelNode(new Label());
        LabelNode end = new LabelNode(new Label());
        if (cn.superName.equals(CLASS_OBJECT)) {
            constructor.instructions = invokeSuperWithoutParameter(start, end);
            constructor.localVariables = createLocalVariables(cn, start, end);
            constructor.maxStack = 1;
        } else {
            constructor.instructions = invokeSuperWithTestableNullParameter(cn, start, end);
            constructor.localVariables = createLocalVariables(cn, start, end);
            constructor.maxStack = 3;
        }
        constructor.maxLocals = 2;
        cn.methods.add(constructor);
    }

    private boolean isUninstantiableClass(ClassNode cn) {
        // if the class has no even default constructor, skip it
        for (MethodNode mn : cn.methods) {
            if (mn.name.equals(CONSTRUCTOR)) {
                return false;
            }
        }
        return true;
    }

    private boolean isInterface(ClassNode cn) {
        // is interface or the object class
        return (cn.access & ACC_INTERFACE) != 0 || cn.superName == null;
    }

    private boolean isJunitTestClass(ClassNode cn) {
        // junit require test class contains only one constructor
        for (MethodNode mn : cn.methods) {
            if (mn.visibleAnnotations == null) {
                continue;
            }
            for (AnnotationNode an : mn.visibleAnnotations) {
                for (String annotation : JUNIT_TEST_ANNOTATIONS) {
                    if (an.desc.equals(annotation)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private InsnList invokeSuperWithoutParameter(LabelNode start, LabelNode end) {
        InsnList il = new InsnList();
        il.add(start);
        il.add(new VarInsnNode(ALOAD, 0));
        il.add(new MethodInsnNode(INVOKESPECIAL, CLASS_OBJECT, CONSTRUCTOR, VOID_METHOD, false));
        il.add(new InsnNode(RETURN));
        il.add(end);
        return il;
    }

    private InsnList invokeSuperWithTestableNullParameter(ClassNode cn, LabelNode start, LabelNode end) {
        InsnList il = new InsnList();
        il.add(start);
        il.add(new VarInsnNode(ALOAD, 0));
        il.add(new TypeInsnNode(NEW, ClassUtil.toSlashSeparatedName(TESTABLE_NULL_TYPE)));
        il.add(new InsnNode(DUP));
        il.add(new MethodInsnNode(INVOKESPECIAL, ClassUtil.toSlashSeparatedName(TESTABLE_NULL_TYPE), CONSTRUCTOR,
            VOID_METHOD, false));
        il.add(new MethodInsnNode(INVOKESPECIAL, cn.superName, CONSTRUCTOR,
            METHOD_START + ClassUtil.toByteCodeClassName(NULL_TYPE) + VOID_METHOD_END, false));
        il.add(new InsnNode(RETURN));
        il.add(end);
        return il;
    }

    private List<LocalVariableNode> createLocalVariables(ClassNode cn, LabelNode start, LabelNode end) {
        return CollectionUtil.listOf(
            new LocalVariableNode(THIS_REF, ClassUtil.toByteCodeClassName(cn.name), null, start, end, 0),
            new LocalVariableNode(IGNORE, ClassUtil.toByteCodeClassName(NULL_TYPE), null, start, end, 1)
        );
    }

}
