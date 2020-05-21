package com.alibaba.testable.translator;

import com.alibaba.testable.model.TestLibType;
import com.alibaba.testable.model.TestableContext;
import com.alibaba.testable.util.ConstPool;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.tree.JCTree.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Travel AST
 *
 * @author flin
 */
public class TestableClassTestRoleTranslator extends TreeTranslator {

    private static final String ANNOTATION_TESTABLE_INJECT = "com.alibaba.testable.annotation.TestableInject";
    private static final String ANNOTATION_JUNIT5_SETUP = "org.junit.jupiter.api.BeforeEach";
    private static final String ANNOTATION_JUNIT5_TEST = "org.junit.jupiter.api.Test";
    private static final String TYPE_CLASS = "Class";
    private final TestableContext cx;
    private String sourceClassName = "";
    private final ListBuffer<Name> sourceClassIns = new ListBuffer();
    private final ListBuffer<String> stubbornFields = new ListBuffer();
    private final ListBuffer<Method> memberMethods = new ListBuffer();

    /**
     * MethodName -> (ResultType -> ParameterTypes)
     */
    private ListBuffer<Pair<Name, Pair<JCExpression, List<JCExpression>>>> injectMethods = new ListBuffer<>();
    private String testSetupMethodName = "";
    private TestLibType testLibType = TestLibType.JUnit4;

    public TestableClassTestRoleTranslator(String pkgName, String className, TestableContext cx) {
        this.sourceClassName = className;
        this.cx = cx;
        try {
            Class<?> cls = Class.forName(pkgName + "." + className);
            Field[] fields = cls.getDeclaredFields();
            for (Field f : fields) {
                if (Modifier.isFinal(f.getModifiers()) || Modifier.isPrivate(f.getModifiers())) {
                    stubbornFields.add(f.getName());
                }
            }
            memberMethods.addAll(Arrays.asList(cls.getDeclaredMethods()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void visitVarDef(JCVariableDecl jcVariableDecl) {
        super.visitVarDef(jcVariableDecl);
        if (((JCIdent)jcVariableDecl.vartype).name.toString().equals(sourceClassName)) {
            jcVariableDecl.vartype = getTestableClassIdent(jcVariableDecl.vartype);
            sourceClassIns.add(jcVariableDecl.name);
        }
    }

    @Override
    public void visitNewClass(JCNewClass jcNewClass) {
        super.visitNewClass(jcNewClass);
        if (getSimpleClassName(jcNewClass).equals(sourceClassName)) {
            jcNewClass.clazz = getTestableClassIdent(jcNewClass.clazz);
        }
    }

    private String getSimpleClassName(JCNewClass jcNewClass) {
        if (jcNewClass.clazz.getClass().equals(JCIdent.class)) {
            return ((JCIdent)jcNewClass.clazz).name.toString();
        } else if (jcNewClass.clazz.getClass().equals(JCFieldAccess.class)) {
            return ((JCFieldAccess)jcNewClass.clazz).name.toString();
        } else {
            return "";
        }
    }

    @Override
    public void visitExec(JCExpressionStatement jcExpressionStatement) {
        if (jcExpressionStatement.expr.getClass().equals(JCAssign.class) &&
            isAssignStubbornField((JCAssign)jcExpressionStatement.expr)) {
            JCAssign assign = (JCAssign)jcExpressionStatement.expr;
            JCFieldAccess stubbornSetter = cx.treeMaker.Select(((JCFieldAccess)assign.lhs).selected,
                getStubbornSetterMethodName(assign));
            jcExpressionStatement.expr = cx.treeMaker.Apply(List.<JCExpression>nil(), stubbornSetter,
                com.sun.tools.javac.util.List.of(assign.rhs));
        }
        super.visitExec(jcExpressionStatement);
    }

    @Override
    public void visitMethodDef(JCMethodDecl jcMethodDecl) {
        for (JCAnnotation a : jcMethodDecl.mods.annotations) {
            switch (a.type.tsym.toString()) {
                case ANNOTATION_TESTABLE_INJECT:
                    ListBuffer<JCExpression> args = new ListBuffer<>();
                    for (JCVariableDecl p : jcMethodDecl.params) {
                        args.add(cx.treeMaker.Select(p.vartype, cx.names.fromString(ConstPool.TYPE_TO_CLASS)));
                    }
                    JCExpression retType = jcMethodDecl.restype == null ? null :
                        cx.treeMaker.Select(jcMethodDecl.restype, cx.names.fromString(ConstPool.TYPE_TO_CLASS));
                    injectMethods.add(Pair.of(jcMethodDecl.name, Pair.of(retType, args.toList())));
                    break;
                case ANNOTATION_JUNIT5_SETUP:
                    testSetupMethodName = jcMethodDecl.name.toString();
                    jcMethodDecl.mods.annotations = removeAnnotation(jcMethodDecl.mods.annotations,
                        ANNOTATION_JUNIT5_SETUP);
                    break;
                case ANNOTATION_JUNIT5_TEST:
                    testLibType = TestLibType.JUnit5;
                    break;
                default:
            }
        }
        super.visitMethodDef(jcMethodDecl);
    }

    @Override
    public void visitClassDef(JCClassDecl jcClassDecl) {
        super.visitClassDef(jcClassDecl);
        ListBuffer<JCTree> ndefs = new ListBuffer<>();
        ndefs.addAll(jcClassDecl.defs);
        JCModifiers mods = cx.treeMaker.Modifiers(Modifier.PUBLIC, makeAnnotations(ANNOTATION_JUNIT5_SETUP));
        ndefs.add(cx.treeMaker.MethodDef(mods, cx.names.fromString("testableSetup"),
            cx.treeMaker.Type(new Type.JCVoidType()), List.<JCTypeParameter>nil(),
            List.<JCVariableDecl>nil(), List.<JCExpression>nil(), testableSetupBlock(), null));
        jcClassDecl.defs = ndefs.toList();
    }

    /**
     * For break point
     */
    @Override
    public void visitAssign(JCAssign jcAssign) {
        super.visitAssign(jcAssign);
    }

    /**
     * For break point
     */
    @Override
    public void visitSelect(JCFieldAccess jcFieldAccess) {
        super.visitSelect(jcFieldAccess);
    }

    private List<JCAnnotation> makeAnnotations(String fullAnnotationName) {
        JCExpression setupAnnotation = nameToExpression(fullAnnotationName);
        return List.of(cx.treeMaker.Annotation(setupAnnotation, List.<JCExpression>nil()));
    }

    private JCExpression nameToExpression(String dotName) {
        String[] nameParts = dotName.split("\\.");
        JCExpression e = cx.treeMaker.Ident(cx.names.fromString(nameParts[0]));
        for (int i = 1; i < nameParts.length; i++) {
            e = cx.treeMaker.Select(e, cx.names.fromString(nameParts[i]));
        }
        return e;
    }

    private JCBlock testableSetupBlock() {
        ListBuffer<JCStatement> statements = new ListBuffer<>();
        for (Pair<Name, Pair<JCExpression, List<JCExpression>>> m : injectMethods.toList()) {
            if (isMemberMethod(m)) {
                statements.append(toGlobalInvokeStatement(m));
            } else {
                statements.append(toGlobalNewStatement(m));
            }
        }
        if (!testSetupMethodName.isEmpty()) {
            statements.append(cx.treeMaker.Exec(cx.treeMaker.Apply(List.<JCExpression>nil(),
                nameToExpression(testSetupMethodName), List.<JCExpression>nil())));
        }
        return cx.treeMaker.Block(0, statements.toList());
    }

    private boolean isMemberMethod(Pair<Name, Pair<JCExpression, List<JCExpression>>> m) {
        for (Method method : memberMethods) {
            if (method.getName().equals(m.fst.toString()) && parameterEquals(m.snd.snd, method.getParameterTypes())) {
                return true;
            }
        }
        return false;
    }

    private boolean parameterEquals(List<JCExpression> injectMethodArgs, Class<?>[] memberMethodArgs) {
        if (injectMethodArgs.length() != memberMethodArgs.length) {
            return false;
        }
        for (int i = 0; i < injectMethodArgs.length(); i++) {
            if (!memberMethodArgs[i].getName().equals(((JCFieldAccess)injectMethodArgs.get(i)).selected.type
                .toString())) {
                return false;
            }
        }
        return true;
    }

    private JCStatement toGlobalInvokeStatement(Pair<Name, Pair<JCExpression, List<JCExpression>>> m) {
        JCExpression key = nameToExpression(ConstPool.NE_X_KEY);
        JCExpression value = nameToExpression(ConstPool.NE_X_VAL);
        JCExpression methodName = cx.treeMaker.Literal(m.fst.toString());
        JCExpression thisIns = cx.treeMaker.Ident(cx.names.fromString(ConstPool.REF_THIS));
        JCExpression returnClassType = m.snd.fst;
        JCExpression parameterTypes = cx.treeMaker.NewArray(cx.treeMaker.Ident(cx.names.fromString(TYPE_CLASS)),
            List.<JCExpression>nil(), m.snd.snd);
        JCNewClass keyClass = cx.treeMaker.NewClass(null, List.<JCExpression>nil(), key,
            List.of(methodName, parameterTypes), null);
        JCNewClass valClass = cx.treeMaker.NewClass(null, List.<JCExpression>nil(), value,
            List.of(thisIns, returnClassType), null);
        JCExpression addInjectMethod = nameToExpression(ConstPool.NE_X_ADD);
        JCMethodInvocation apply = cx.treeMaker.Apply(List.<JCExpression>nil(), addInjectMethod,
            List.from(new JCExpression[] {keyClass, valClass}));
        return cx.treeMaker.Exec(apply);
    }

    private JCStatement toGlobalNewStatement(Pair<Name, Pair<JCExpression, List<JCExpression>>> m) {
        JCExpression key = nameToExpression(ConstPool.NE_W_KEY);
        JCExpression value = nameToExpression(ConstPool.NE_W_VAL);
        JCExpression classType = m.snd.fst;
        JCExpression parameterTypes = cx.treeMaker.NewArray(cx.treeMaker.Ident(cx.names.fromString(TYPE_CLASS)),
            List.<JCExpression>nil(), m.snd.snd);
        JCExpression thisIns = cx.treeMaker.Ident(cx.names.fromString(ConstPool.REF_THIS));
        JCExpression methodName = cx.treeMaker.Literal(m.fst.toString());
        JCNewClass keyClass = cx.treeMaker.NewClass(null, List.<JCExpression>nil(), key,
            List.of(classType, parameterTypes), null);
        JCNewClass valClass = cx.treeMaker.NewClass(null, List.<JCExpression>nil(), value,
            List.of(thisIns, methodName), null);
        JCExpression addInjectMethod = nameToExpression(ConstPool.NE_W_ADD);
        JCMethodInvocation apply = cx.treeMaker.Apply(List.<JCExpression>nil(), addInjectMethod,
            List.from(new JCExpression[] {keyClass, valClass}));
        return cx.treeMaker.Exec(apply);
    }

    private List<JCAnnotation> removeAnnotation(List<JCAnnotation> annotations, String target) {
        ListBuffer<JCAnnotation> nb = new ListBuffer<>();
        for (JCAnnotation i : annotations) {
            if (!i.type.tsym.toString().equals(target)) {
                nb.add(i);
            }
        }
        return nb.toList();
    }

    private Name getStubbornSetterMethodName(JCAssign assign) {
        String name = ((JCFieldAccess)assign.lhs).name.toString() + ConstPool.TESTABLE_SET_METHOD_PREFIX;
        return cx.names.fromString(name);
    }

    private boolean isAssignStubbornField(JCAssign expr) {
        return expr.lhs.getClass().equals(JCFieldAccess.class) &&
            sourceClassIns.contains(((JCIdent)((JCFieldAccess)(expr).lhs).selected).name) &&
            stubbornFields.contains(((JCFieldAccess)(expr).lhs).name.toString());
    }

    private JCIdent getTestableClassIdent(JCExpression clazz) {
        Name className = ((JCIdent)clazz).name;
        return cx.treeMaker.Ident(cx.names.fromString(className + ConstPool.TESTABLE));
    }

}
