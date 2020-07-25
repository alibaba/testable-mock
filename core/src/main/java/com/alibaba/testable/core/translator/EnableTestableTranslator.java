package com.alibaba.testable.core.translator;

import com.alibaba.testable.core.generator.PrivateAccessStatementGenerator;
import com.alibaba.testable.core.generator.TestSetupMethodGenerator;
import com.alibaba.testable.core.model.TestableContext;
import com.alibaba.testable.core.util.ConstPool;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Travel AST
 *
 * @author flin
 */
public class EnableTestableTranslator extends BaseTranslator {

    private final TestableContext cx;
    private String sourceClassName = "";
    private final ListBuffer<Name> sourceClassIns = new ListBuffer<>();
    private final ListBuffer<String> privateOrFinalFields = new ListBuffer<>();
    private final ListBuffer<String> privateMethods = new ListBuffer<>();
    private final TestSetupMethodGenerator testSetupMethodGenerator;
    private final PrivateAccessStatementGenerator privateAccessStatementGenerator;

    public EnableTestableTranslator(String pkgName, String className, TestableContext cx) {
        this.sourceClassName = className;
        this.cx = cx;
        this.testSetupMethodGenerator = new TestSetupMethodGenerator(cx);
        this.privateAccessStatementGenerator = new PrivateAccessStatementGenerator(cx);
        try {
            Class<?> cls = Class.forName(pkgName + "." + className);
            Field[] fields = cls.getDeclaredFields();
            for (Field f : fields) {
                if (Modifier.isFinal(f.getModifiers()) || Modifier.isPrivate(f.getModifiers())) {
                    privateOrFinalFields.add(f.getName());
                }
            }
            Method[] methods = cls.getDeclaredMethods();
            for (Method m : methods) {
                if (Modifier.isPrivate(m.getModifiers())) {
                    privateMethods.add(m.getName());
                }
            }
            testSetupMethodGenerator.memberMethods.addAll(Arrays.asList(cls.getDeclaredMethods()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void visitVarDef(JCVariableDecl jcVariableDecl) {
        super.visitVarDef(jcVariableDecl);
        if (jcVariableDecl.vartype.getClass().equals(JCIdent.class) &&
            ((JCIdent)jcVariableDecl.vartype).name.toString().equals(sourceClassName)) {
            sourceClassIns.add(jcVariableDecl.name);
        }
    }

    /**
     * d.privateField = val -> PrivateAccessor.set(d, "privateField", val)
     * d.privateMethod(args) -> PrivateAccessor.invoke(d, "privateMethod", args)
     */
    @Override
    public void visitExec(JCExpressionStatement jcExpressionStatement) {
        if (jcExpressionStatement.expr.getClass().equals(JCAssign.class) &&
            isPrivateField((JCAssign)jcExpressionStatement.expr)) {
            jcExpressionStatement.expr = privateAccessStatementGenerator.fetchSetterStatement(
                (JCAssign)jcExpressionStatement.expr);
        }
        jcExpressionStatement.expr = checkAndExchange(jcExpressionStatement.expr);
        super.visitExec(jcExpressionStatement);
    }

    /**
     * Search for TestableInject and TestSetup annotations
     */
    @Override
    public void visitMethodDef(JCMethodDecl jcMethodDecl) {
        for (JCAnnotation a : jcMethodDecl.mods.annotations) {
            if (ConstPool.ANNOTATION_TESTABLE_INJECT.equals(a.type.tsym.toString())) {
                ListBuffer<JCExpression> args = new ListBuffer<>();
                for (JCVariableDecl p : jcMethodDecl.params) {
                    args.add(cx.treeMaker.Select(p.vartype, cx.names.fromString(ConstPool.CLASS_OF_TYPE)));
                }
                JCExpression retType = jcMethodDecl.restype == null ? null :
                    cx.treeMaker.Select(jcMethodDecl.restype, cx.names.fromString(ConstPool.CLASS_OF_TYPE));
                testSetupMethodGenerator.injectMethods.add(Pair.of(jcMethodDecl.name, Pair.of(retType, args.toList())));
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
     * For private invoke invocation break point
     */
    @Override
    public void visitApply(JCMethodInvocation tree) {
        tree.args = checkAndExchange(tree.args);
        super.visitApply(tree);
    }

    /**
     * For private setter break point
     */
    @Override
    public void visitAssign(JCAssign jcAssign) {
        super.visitAssign(jcAssign);
    }

    /**
     * For private getter break point
     */
    @Override
    public void visitSelect(JCFieldAccess jcFieldAccess) {
        super.visitSelect(jcFieldAccess);
    }

    @Override
    protected JCExpression checkAndExchange(JCExpression expr) {
        if (expr.getClass().equals(JCMethodInvocation.class) &&
            isPrivateMethod((JCMethodInvocation)expr)) {
            expr = privateAccessStatementGenerator.fetchInvokeStatement((JCMethodInvocation)expr);
        }
        return expr;
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

    private boolean isPrivateField(JCAssign expr) {
        return expr.lhs.getClass().equals(JCFieldAccess.class) &&
            ((JCFieldAccess)(expr).lhs).selected.getClass().equals(JCIdent.class) &&
            sourceClassIns.contains(((JCIdent)((JCFieldAccess)(expr).lhs).selected).name) &&
            privateOrFinalFields.contains(((JCFieldAccess)(expr).lhs).name.toString());
    }

    private boolean isPrivateMethod(JCMethodInvocation expr) {
        return expr.meth.getClass().equals(JCFieldAccess.class) &&
            ((JCFieldAccess)(expr).meth).selected.getClass().equals(JCIdent.class) &&
            sourceClassIns.contains(((JCIdent)((JCFieldAccess)(expr).meth).selected).name) &&
            privateMethods.contains(((JCFieldAccess)(expr).meth).name.toString());
    }

}
