package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.handler.test.JUnit4Framework;
import com.alibaba.testable.agent.handler.test.JUnit5Framework;
import com.alibaba.testable.agent.util.AnnotationUtil;
import com.alibaba.testable.agent.util.ClassUtil;
import com.alibaba.testable.core.util.CollectionUtil;
import com.alibaba.testable.core.util.StringUtil;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;
import java.util.Map;

import static com.alibaba.testable.agent.constant.ConstPool.CLASS_OBJECT;
import static com.alibaba.testable.core.constant.ConstPool.CONSTRUCTOR;
import static com.alibaba.testable.core.constant.ConstPool.THIS_REF;
import static com.alibaba.testable.core.util.CollectionUtil.*;

/**
 * @author flin
 */
public class OmniClassHandler extends BaseClassHandler {

    private static final String VOID_TYPE = "java/lang/Void";
    private static final String IGNORE = "ignore";
    private static final String METHOD_START = "(";
    private static final String VOID_METHOD_END = ")V";
    private static final String VOID_METHOD = "()V";
    private static final String ENABLE_CONFIGURATION = "org.springframework.context.annotation.Configuration";
    private static final String CLASS_OMNI_CONSTRUCTOR = "com/alibaba/testable/core/tool/OmniConstructor";
    public static final String CLASS_CONSTRUCTION_OPTION = "com/alibaba/testable/core/model/ConstructionOption";
    public static final String METHOD_NEW_INSTANCE = "newInstance";
    public static final String METHOD_DESC_NEW_INSTANCE = "(Ljava/lang/Class;[Lcom/alibaba/testable/core/model/ConstructionOption;)Ljava/lang/Object;";

    private static final String CLASS_ABSTRACT_COLLECTION = "java/util/AbstractCollection";
    private static final String CLASS_NUMBER = "java/lang/Number";
    private static final String CLASS_HASH_SET = "java/util/HashSet";
    private static final String CLASS_READER = "java/io/Reader";
    private static final String CLASS_WRITER = "java/io/Writer";
    private static final String CLASS_BUFFERED_READER = "java/io/BufferedReader";
    private static final String CLASS_BUFFERED_WRITER = "java/io/BufferedWriter";
    private static final String CLASS_INPUT_STREAM = "java/io/InputStream";
    private static final String CLASS_OUTPUT_STREAM = "java/io/OutputStream";
    private static final String CLASS_BUFFERED_INPUT_STREAM = "java/io/BufferedInputStream";
    private static final String CLASS_BUFFERED_OUTPUT_STREAM = "java/io/BufferedOutputStream";
    private static final String CLASS_THREAD = "java/lang/Thread";
    private static final String CLASS_FILE = "java/io/File";

    // below classes are loaded before OmniClassHandler, cannot be instrumented
    // map of class name to constructor parameters
    private static final Map<String, String[]> PRELOADED_CLASSES = mapOf(
            entryOf(CLASS_OBJECT, CollectionUtil.<String>arrayOf()),
            entryOf(CLASS_READER, CollectionUtil.<String>arrayOf()),
            entryOf(CLASS_WRITER, CollectionUtil.<String>arrayOf()),
            entryOf(CLASS_INPUT_STREAM, CollectionUtil.<String>arrayOf()),
            entryOf(CLASS_OUTPUT_STREAM, CollectionUtil.<String>arrayOf()),
            entryOf(CLASS_THREAD, CollectionUtil.<String>arrayOf()),
            entryOf(CLASS_FILE, arrayOf("Ljava/lang/String;")),
            entryOf(CLASS_BUFFERED_READER, arrayOf("Ljava/io/Reader;")),
            entryOf(CLASS_BUFFERED_WRITER, arrayOf("Ljava/io/Reader;")),
            entryOf(CLASS_BUFFERED_INPUT_STREAM, arrayOf("Ljava/io/Reader;")),
            entryOf(CLASS_BUFFERED_OUTPUT_STREAM, arrayOf("Ljava/io/Reader;"))
    );

    private static final String[] JUNIT_TEST_ANNOTATIONS = new String[] {
        JUnit4Framework.ANNOTATION_TEST, JUnit5Framework.ANNOTATION_TEST, JUnit5Framework.ANNOTATION_PARAMETERIZED_TEST
    };
    private static final String[] UNREACHABLE_CLASSES = new String[] {
        CLASS_ABSTRACT_COLLECTION, CLASS_NUMBER, CLASS_HASH_SET
    };

    @Override
    protected void transform(ClassNode cn) {
        if (isInterfaceOrAtom(cn) || isUniqueConstructorClass(cn) || isUninstantiableClass(cn) ||
                AnnotationUtil.getClassAnnotation(cn, ENABLE_CONFIGURATION) != null) {
            return;
        }
        addConstructorWithVoidTypeParameter(cn);
    }

    private void addConstructorWithVoidTypeParameter(ClassNode cn) {
        MethodNode constructor = new MethodNode(ACC_PUBLIC, CONSTRUCTOR,
            METHOD_START + ClassUtil.toByteCodeClassName(VOID_TYPE) + VOID_METHOD_END, null, null);
        LabelNode start = new LabelNode(new Label());
        LabelNode end = new LabelNode(new Label());
        if (PRELOADED_CLASSES.containsKey(cn.superName)) {
            constructor.instructions = invokeSuperWithoutParameter(cn.superName, start, end);
            constructor.localVariables = createLocalVariables(cn, start, end);
            constructor.maxStack = 1 + PRELOADED_CLASSES.get(cn.superName).length;
        } else {
            constructor.instructions = invokeSuperWithTestableVoidParameter(cn.superName, start, end);
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

    private boolean isInterfaceOrAtom(ClassNode cn) {
        // is interface, Object class or Void class
        return (cn.access & ACC_INTERFACE) != 0 || cn.superName == null || VOID_TYPE.equals(cn.name);
    }

    private boolean isUniqueConstructorClass(ClassNode cn) {
        // elastic plugin class should contain only one constructor
        if ("org/elasticsearch/plugins/Plugin".equals(cn.superName)) {
            return true;
        }
        // should skip mockito generated class
        if (cn.name.contains("$MockitoMock$")) {
            return true;
        }
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

    private InsnList invokeSuperWithoutParameter(String superName, LabelNode start, LabelNode end) {
        String[] parameters = PRELOADED_CLASSES.get(superName);
        InsnList il = new InsnList();
        il.add(start);
        il.add(new VarInsnNode(ALOAD, 0));
        for (String p : parameters) {
            il.add(new LdcInsnNode(Type.getType(p)));
            il.add(new InsnNode(ICONST_0));
            il.add(new TypeInsnNode(ANEWARRAY, CLASS_CONSTRUCTION_OPTION));
            il.add(new MethodInsnNode(INVOKESTATIC, CLASS_OMNI_CONSTRUCTOR, METHOD_NEW_INSTANCE, METHOD_DESC_NEW_INSTANCE, false));
            il.add(new TypeInsnNode(CHECKCAST, ClassUtil.toSlashSeparateJavaStyleName(p)));
        }
        il.add(new MethodInsnNode(INVOKESPECIAL, superName, CONSTRUCTOR,
                METHOD_START + StringUtil.join("", parameters) + VOID_METHOD_END, false));
        il.add(new InsnNode(RETURN));
        il.add(end);
        return il;
    }

    private InsnList invokeSuperWithTestableVoidParameter(String superName, LabelNode start, LabelNode end) {
        InsnList il = new InsnList();
        il.add(start);
        il.add(new VarInsnNode(ALOAD, 0));
        if (contains(UNREACHABLE_CLASSES, superName)) {
            il.add(new MethodInsnNode(INVOKESPECIAL, superName, CONSTRUCTOR, VOID_METHOD, false));
        } else {
            il.add(new VarInsnNode(ALOAD, 1));
            il.add(new MethodInsnNode(INVOKESPECIAL, superName, CONSTRUCTOR,
                METHOD_START + ClassUtil.toByteCodeClassName(VOID_TYPE) + VOID_METHOD_END, false));
        }
        il.add(new InsnNode(RETURN));
        il.add(end);
        return il;
    }

    private List<LocalVariableNode> createLocalVariables(ClassNode cn, LabelNode start, LabelNode end) {
        return CollectionUtil.mutableListOf(
            new LocalVariableNode(THIS_REF, ClassUtil.toByteCodeClassName(cn.name), null, start, end, 0),
            new LocalVariableNode(IGNORE, ClassUtil.toByteCodeClassName(VOID_TYPE), null, start, end, 1)
        );
    }

}
