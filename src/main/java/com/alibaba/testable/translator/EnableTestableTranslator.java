package com.alibaba.testable.translator;

import com.alibaba.testable.generator.TestSetupMethodGenerator;
import com.alibaba.testable.model.TestLibType;
import com.alibaba.testable.model.TestableContext;
import com.alibaba.testable.util.ConstPool;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.tree.JCTree.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Travel AST
 *
 * @author flin
 */
public class EnableTestableTranslator extends TreeTranslator {

    private final TestableContext cx;
    private String sourceClassName = "";
    private final ListBuffer<Name> sourceClassIns = new ListBuffer<>();
    private final ListBuffer<String> stubbornFields = new ListBuffer<>();
    private final TestSetupMethodGenerator testSetupMethodGenerator;

    public EnableTestableTranslator(String pkgName, String className, TestableContext cx) {
        this.sourceClassName = className;
        this.cx = cx;
        this.testSetupMethodGenerator = new TestSetupMethodGenerator(cx);
        try {
            Class<?> cls = Class.forName(pkgName + "." + className);
            Field[] fields = cls.getDeclaredFields();
            for (Field f : fields) {
                if (Modifier.isFinal(f.getModifiers()) || Modifier.isPrivate(f.getModifiers())) {
                    stubbornFields.add(f.getName());
                }
            }
            testSetupMethodGenerator.memberMethods.addAll(Arrays.asList(cls.getDeclaredMethods()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Demo d = new Demo() -> DemoTestable d = new Demo()
     */
    @Override
    public void visitVarDef(JCVariableDecl jcVariableDecl) {
        super.visitVarDef(jcVariableDecl);
        if (jcVariableDecl.vartype.getClass().equals(JCIdent.class) &&
            ((JCIdent)jcVariableDecl.vartype).name.toString().equals(sourceClassName)) {
            jcVariableDecl.vartype = getTestableClassIdent(jcVariableDecl.vartype);
            sourceClassIns.add(jcVariableDecl.name);
        }
    }

    /**
     * Demo d = new Demo() -> Demo d = new DemoTestable()
     */
    @Override
    public void visitNewClass(JCNewClass jcNewClass) {
        super.visitNewClass(jcNewClass);
        if (getSimpleClassName(jcNewClass).equals(sourceClassName)) {
            jcNewClass.clazz = getTestableClassIdent(jcNewClass.clazz);
        }
    }

    /**
     * d.privateField = val -> d.privateFieldTestableSet(val)
     */
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

    /**
     * Search for TestableInject and TestSetup annotations
     */
    @Override
    public void visitMethodDef(JCMethodDecl jcMethodDecl) {
        for (JCAnnotation a : jcMethodDecl.mods.annotations) {
            switch (a.type.tsym.toString()) {
                case ConstPool.ANNOTATION_TESTABLE_INJECT:
                    ListBuffer<JCExpression> args = new ListBuffer<>();
                    for (JCVariableDecl p : jcMethodDecl.params) {
                        args.add(cx.treeMaker.Select(p.vartype, cx.names.fromString(ConstPool.TYPE_TO_CLASS)));
                    }
                    JCExpression retType = jcMethodDecl.restype == null ? null :
                        cx.treeMaker.Select(jcMethodDecl.restype, cx.names.fromString(ConstPool.TYPE_TO_CLASS));
                    testSetupMethodGenerator.injectMethods.add(Pair.of(jcMethodDecl.name, Pair.of(retType, args.toList())));
                    break;
                case ConstPool.ANNOTATION_JUNIT5_SETUP:
                    testSetupMethodGenerator.testSetupMethodName = jcMethodDecl.name.toString();
                    jcMethodDecl.mods.annotations = removeAnnotation(jcMethodDecl.mods.annotations,
                        ConstPool.ANNOTATION_JUNIT5_SETUP);
                    break;
                case ConstPool.ANNOTATION_JUNIT5_TEST:
                    testSetupMethodGenerator.testLibType = TestLibType.JUnit5;
                    break;
                default:
            }
        }
        super.visitMethodDef(jcMethodDecl);
    }

    /**
     * Generate test setup method to initialize n.e.pool
     */
    @Override
    public void visitClassDef(JCClassDecl jcClassDecl) {
        super.visitClassDef(jcClassDecl);
        ListBuffer<JCTree> ndefs = new ListBuffer<>();
        ndefs.addAll(jcClassDecl.defs);
        ndefs.add(testSetupMethodGenerator.fetch());
        jcClassDecl.defs = ndefs.toList();
    }

    /**
     * For setter break point
     */
    @Override
    public void visitAssign(JCAssign jcAssign) {
        super.visitAssign(jcAssign);
    }

    /**
     * For getter break point
     */
    @Override
    public void visitSelect(JCFieldAccess jcFieldAccess) {
        super.visitSelect(jcFieldAccess);
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
