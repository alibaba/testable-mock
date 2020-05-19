package com.alibaba.testable.translator;

import com.alibaba.testable.model.TestLibType;
import com.alibaba.testable.model.TestableContext;
import com.alibaba.testable.translator.tree.TestableFieldAccess;
import com.alibaba.testable.translator.tree.TestableMethodInvocation;
import com.alibaba.testable.util.ConstPool;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.*;

import java.lang.reflect.Modifier;

/**
 * Travel AST
 *
 * @author flin
 */
public class TestableClassTestRoleTranslator extends TreeTranslator {

    private static final String ANNOTATION_TESTABLE_INJECT = "com.alibaba.testable.annotation.TestableInject";
    private static final String ANNOTATION_JUNIT5_SETUP = "org.junit.jupiter.api.BeforeEach";
    private static final String ANNOTATION_JUNIT5_TEST = "org.junit.jupiter.api.Test";
    private final TestableContext cx;
    private String sourceClassName;
    private ListBuffer<Name> sourceClassIns = new ListBuffer();
    private List<String> stubbornFields = List.nil();
    private ListBuffer<Pair<Type, Pair<Name, List<Type>>>> injectMethods = new ListBuffer<>();
    private String testSetupMethodName;
    private TestLibType testLibType = TestLibType.JUnit4;

    public TestableClassTestRoleTranslator(String pkgName, String className, TestableContext cx) {
        this.sourceClassName = className;
        this.cx = cx;
        try {
            stubbornFields = List.from(
                (String[])Class.forName(pkgName + "." + className + ConstPool.TESTABLE)
                .getMethod(ConstPool.STUBBORN_FIELD_METHOD)
                .invoke(null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void visitVarDef(JCTree.JCVariableDecl jcVariableDecl) {
        super.visitVarDef(jcVariableDecl);
        if (((JCTree.JCIdent)jcVariableDecl.vartype).name.toString().equals(sourceClassName)) {
            jcVariableDecl.vartype = getTestableClassIdent(jcVariableDecl.vartype);
            sourceClassIns.add(jcVariableDecl.name);
        }
    }

    @Override
    public void visitNewClass(JCTree.JCNewClass jcNewClass) {
        super.visitNewClass(jcNewClass);
        if (((JCTree.JCIdent)jcNewClass.clazz).name.toString().equals(sourceClassName)) {
            jcNewClass.clazz = getTestableClassIdent(jcNewClass.clazz);
        }
    }

    @Override
    public void visitExec(JCTree.JCExpressionStatement jcExpressionStatement) {
        if (jcExpressionStatement.expr.getClass().equals(JCTree.JCAssign.class) &&
            isAssignStubbornField((JCTree.JCAssign)jcExpressionStatement.expr)) {
            JCTree.JCAssign assign = (JCTree.JCAssign)jcExpressionStatement.expr;
            // TODO: Use treeMaker.Apply() and treeMaker.Select()
            TestableFieldAccess stubbornSetter = new TestableFieldAccess(((JCTree.JCFieldAccess)assign.lhs).selected,
                getStubbornSetterMethodName(assign), null);
            jcExpressionStatement.expr = new TestableMethodInvocation(null, stubbornSetter,
                com.sun.tools.javac.util.List.of(assign.rhs));
        }
        super.visitExec(jcExpressionStatement);
    }

    @Override
    public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
        for (JCTree.JCAnnotation a : jcMethodDecl.mods.annotations) {
            switch (a.type.tsym.toString()) {
                case ANNOTATION_TESTABLE_INJECT:
                    ListBuffer<Type> args = new ListBuffer<>();
                    for (JCTree.JCVariableDecl p : jcMethodDecl.params) {
                        args.add(p.vartype.type);
                    }
                    injectMethods.add(Pair.of(jcMethodDecl.restype.type, Pair.of(jcMethodDecl.name, args.toList())));
                    break;
                case ANNOTATION_JUNIT5_SETUP:
                    testSetupMethodName = jcMethodDecl.name.toString();
                    jcMethodDecl.mods.annotations = removeAnnotation(jcMethodDecl.mods.annotations, ANNOTATION_JUNIT5_SETUP);
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
    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
        super.visitClassDef(jcClassDecl);
        ListBuffer<JCTree> ndefs = new ListBuffer<>();
        ndefs.addAll(jcClassDecl.defs);
        JCTree.JCModifiers mods = cx.treeMaker.Modifiers(Modifier.PUBLIC, makeAnnotations());
        ndefs.add(cx.treeMaker.MethodDef(mods, cx.names.fromString("testableSetup"),
            cx.treeMaker.Type(new Type.JCVoidType()), List.<JCTree.JCTypeParameter>nil(),
            List.<JCTree.JCVariableDecl>nil(), List.<JCTree.JCExpression>nil(), testableSetupBlock(), null));
        jcClassDecl.defs = ndefs.toList();
    }

    private List<JCTree.JCAnnotation> makeAnnotations() {
        String[] elems = ANNOTATION_JUNIT5_TEST.split("\\.");
        JCTree.JCExpression e = cx.treeMaker.Ident(cx.names.fromString(elems[0]));
        for (int i = 1 ; i < elems.length ; i++) {
            e = cx.treeMaker.Select(e, cx.names.fromString(elems[i]));
        }
        return List.of(cx.treeMaker.Annotation(e, List.<JCTree.JCExpression>nil()));
    }

    private JCTree.JCBlock testableSetupBlock() {
        return cx.treeMaker.Block(0, List.<JCTree.JCStatement>nil());
    }

    /**
     * For break point
     */
    @Override
    public void visitAssign(JCTree.JCAssign jcAssign) {
        super.visitAssign(jcAssign);
    }

    /**
     * For break point
     */
    @Override
    public void visitSelect(JCTree.JCFieldAccess jcFieldAccess) {
        super.visitSelect(jcFieldAccess);
    }

    private List<JCTree.JCAnnotation> removeAnnotation(
        List<JCTree.JCAnnotation> annotations, String target) {
        ListBuffer<JCTree.JCAnnotation> nb = new ListBuffer<>();
        for (JCTree.JCAnnotation i : annotations) {
            if (!i.type.tsym.toString().equals(target)) {
                nb.add(i);
            }
        }
        return nb.toList();
    }

    private Name getStubbornSetterMethodName(JCTree.JCAssign assign) {
        String name = ((JCTree.JCFieldAccess)assign.lhs).name.toString() + ConstPool.TESTABLE_SET_METHOD_PREFIX;
        return cx.names.fromString(name);
    }

    private boolean isAssignStubbornField(JCTree.JCAssign expr) {
        return expr.lhs.getClass().equals(JCTree.JCFieldAccess.class) &&
            sourceClassIns.contains(((JCTree.JCIdent)((JCTree.JCFieldAccess)(expr).lhs).selected).name) &&
            stubbornFields.contains(((JCTree.JCFieldAccess)(expr).lhs).name.toString());
    }

    private JCTree.JCIdent getTestableClassIdent(JCTree.JCExpression clazz) {
        Name className = ((JCTree.JCIdent)clazz).name;
        return cx.treeMaker.Ident(cx.names.fromString(className + ConstPool.TESTABLE));
    }

}
