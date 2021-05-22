package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.handler.test.JUnit4Framework;
import com.alibaba.testable.agent.handler.test.JUnit5Framework;
import com.alibaba.testable.agent.util.ClassUtil;
import com.alibaba.testable.agent.util.CollectionUtil;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

import java.util.List;

import static com.alibaba.testable.agent.constant.ConstPool.CLASS_OBJECT;
import static com.alibaba.testable.core.constant.ConstPool.CONSTRUCTOR;
import static com.alibaba.testable.core.constant.ConstPool.THIS_REF;
import static com.alibaba.testable.core.util.CollectionUtil.contains;

/**
 * @author flin
 */
public class OmniClassHandler extends BaseClassHandler {

    private static final String VOID_TYPE = "java/lang/Void";
    private static final String IGNORE = "ignore";
    private static final String METHOD_START = "(";
    private static final String VOID_METHOD_END = ")V";
    private static final String VOID_METHOD = "()V";
    private static final String ENABLE_CONFIGURATION = "Lorg/springframework/context/annotation/Configuration;";
    private static final String CLASS_ABSTRACT_COLLECTION = "java/util/AbstractCollection";
    private static final String CLASS_NUMBER = "java/lang/Number";
    private static final String CLASS_HASH_SET = "java/util/HashSet";

    private static final String[] JUNIT_TEST_ANNOTATIONS = new String[] {
        JUnit4Framework.ANNOTATION_TEST, JUnit5Framework.ANNOTATION_TEST, JUnit5Framework.ANNOTATION_PARAMETERIZED_TEST
    };
    private static final String[] UNREACHABLE_CLASSES = new String[] {
        CLASS_ABSTRACT_COLLECTION, CLASS_NUMBER, CLASS_HASH_SET
    };

    @Override
    protected void transform(ClassNode cn) {
        if (isInterfaceOrAtom(cn) || isJunitTestClass(cn) || isUninstantiableClass(cn) || hasSpecialAnnotation(cn)) {
            return;
        }
        addConstructorWithVoidTypeParameter(cn);
    }

    private void addConstructorWithVoidTypeParameter(ClassNode cn) {
        MethodNode constructor = new MethodNode(ACC_PUBLIC, CONSTRUCTOR,
            METHOD_START + ClassUtil.toByteCodeClassName(VOID_TYPE) + VOID_METHOD_END, null, null);
        LabelNode start = new LabelNode(new Label());
        LabelNode end = new LabelNode(new Label());
        if (cn.superName.equals(CLASS_OBJECT)) {
            constructor.instructions = invokeSuperWithoutParameter(start, end);
            constructor.localVariables = createLocalVariables(cn, start, end);
            constructor.maxStack = 1;
        } else {
            constructor.instructions = invokeSuperWithTestableVoidParameter(cn, start, end);
            constructor.localVariables = createLocalVariables(cn, start, end);
            constructor.maxStack = 3;
        }
        constructor.maxLocals = 2;
        cn.methods.add(constructor);
    }

    private boolean hasSpecialAnnotation(ClassNode cn) {
        if (cn.visibleAnnotations == null) {
            return false;
        }
        for (AnnotationNode an : cn.visibleAnnotations) {
            if (an.desc.equals(ENABLE_CONFIGURATION)) {
                return true;
            }
        }
        return false;
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

    private boolean isInterfaceOrAtom(ClassNode cn) {
        // is interface, Object class or Void class
        return (cn.access & ACC_INTERFACE) != 0 || cn.superName == null || VOID_TYPE.equals(cn.name);
    }

    private boolean isJunitTestClass(ClassNode cn) {
        // junit require test class contains only one constructor
        for (MethodNode mn : cn.methods) {
            if (mn.visibleAnnotations == null) {
                continue;
            }
            for (AnnotationNode an : mn.visibleAnnotations) {
                if (contains(JUNIT_TEST_ANNOTATIONS, an.desc)) {
                    return true;
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

    private InsnList invokeSuperWithTestableVoidParameter(ClassNode cn, LabelNode start, LabelNode end) {
        InsnList il = new InsnList();
        il.add(start);
        il.add(new VarInsnNode(ALOAD, 0));
        if (contains(UNREACHABLE_CLASSES, cn.superName)) {
            il.add(new MethodInsnNode(INVOKESPECIAL, cn.superName, CONSTRUCTOR, VOID_METHOD, false));
        } else {
            il.add(new VarInsnNode(ALOAD, 1));
            il.add(new MethodInsnNode(INVOKESPECIAL, cn.superName, CONSTRUCTOR,
                METHOD_START + ClassUtil.toByteCodeClassName(VOID_TYPE) + VOID_METHOD_END, false));
        }
        il.add(new InsnNode(RETURN));
        il.add(end);
        return il;
    }

    private List<LocalVariableNode> createLocalVariables(ClassNode cn, LabelNode start, LabelNode end) {
        return CollectionUtil.listOf(
            new LocalVariableNode(THIS_REF, ClassUtil.toByteCodeClassName(cn.name), null, start, end, 0),
            new LocalVariableNode(IGNORE, ClassUtil.toByteCodeClassName(VOID_TYPE), null, start, end, 1)
        );
    }

}
