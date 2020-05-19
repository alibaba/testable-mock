package com.alibaba.testable.translator;

import com.alibaba.testable.model.TestableContext;
import com.alibaba.testable.translator.tree.TestableFieldAccess;
import com.alibaba.testable.translator.tree.TestableMethodInvocation;
import com.alibaba.testable.util.ConstPool;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import java.lang.reflect.Modifier;

/**
 * Travel AST
 *
 * @author flin
 */
public class TestableClassDevRoleTranslator extends TreeTranslator {

    private final TestableContext cx;

    /**
     * Methods to inject
     */
    private List<JCMethodDecl> methods = List.nil();

    /**
     * Fields to wrap
     */
    private List<JCTree.JCVariableDecl> fields = List.nil();

    public List<JCMethodDecl> getMethods() {
        return methods;
    }

    public List<JCTree.JCVariableDecl> getFields() {
        return fields;
    }

    public TestableClassDevRoleTranslator(TestableContext cx) {
        this.cx = cx;
    }

    @Override
    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
        super.visitClassDef(jcClassDecl);
        jcClassDecl.mods.flags = jcClassDecl.mods.flags & (~Modifier.FINAL);
    }

    /**
     * Record all methods
     */
    @Override
    public void visitMethodDef(JCMethodDecl jcMethodDecl) {
        super.visitMethodDef(jcMethodDecl);
        methods = methods.append(jcMethodDecl);
    }

    /**
     * Case: new Demo()
     */
    @Override
    public void visitExec(JCTree.JCExpressionStatement jcExpressionStatement) {
        jcExpressionStatement.expr = checkAndExchangeNewOperation(jcExpressionStatement.expr);
        super.visitExec(jcExpressionStatement);
    }

    /**
     * Case: call(new Demo())
     */
    @Override
    public void visitApply(JCTree.JCMethodInvocation tree) {
        tree.args = checkAndExchangeNewOperation(tree.args);
        super.visitApply(tree);
    }

    /**
     * Record all private fields
     * Case: Demo d = new Demo()
     */
    @Override
    public void visitVarDef(JCTree.JCVariableDecl jcVariableDecl) {
        if (isStubbornField(jcVariableDecl.mods)) {
            fields = fields.append(jcVariableDecl);
        }
        jcVariableDecl.init = checkAndExchangeNewOperation(jcVariableDecl.init);
        super.visitVarDef(jcVariableDecl);
    }

    /**
     * Case: new Demo().call()
     */
    @Override
    public void visitSelect(JCTree.JCFieldAccess jcFieldAccess) {
        jcFieldAccess.selected = checkAndExchangeNewOperation(jcFieldAccess.selected);
        super.visitSelect(jcFieldAccess);
    }

    /**
     * For break point
     */
    @Override
    public void visitNewClass(JCTree.JCNewClass jcNewClass) {
        super.visitNewClass(jcNewClass);
    }

    /**
     * For break point
     */
    @Override
    public void visitNewArray(JCTree.JCNewArray jcNewArray) {
        super.visitNewArray(jcNewArray);
    }

    private boolean isStubbornField(JCTree.JCModifiers mods) {
        return mods.getFlags().contains(javax.lang.model.element.Modifier.PRIVATE) ||
            mods.getFlags().contains(javax.lang.model.element.Modifier.FINAL);
    }

    private List<JCTree.JCExpression> checkAndExchangeNewOperation(List<JCTree.JCExpression> args) {
        if (args != null) {
            JCTree.JCExpression[] es = new JCTree.JCExpression[args.length()];
            for (int i = 0; i < args.length(); i++) {
                es[i] = checkAndExchangeNewOperation(args.get(i));
            }
            return List.from(es);
        }
        return null;
    }

    private JCTree.JCExpression checkAndExchangeNewOperation(JCTree.JCExpression expr) {
        if (isNewOperation(expr)) {
            JCTree.JCNewClass newClassExpr = (JCTree.JCNewClass)expr;
            Name className = ((JCTree.JCIdent)newClassExpr.clazz).name;
            try {
                return getStaticNewCall(newClassExpr, className);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return expr;
    }

    private boolean isNewOperation(JCTree.JCExpression expr) {
        return expr != null && expr.getClass().equals(JCTree.JCNewClass.class);
    }

    private TestableMethodInvocation getStaticNewCall(JCTree.JCNewClass newClassExpr, Name className) {
        TestableFieldAccess snClass = new TestableFieldAccess(cx.treeMaker.Ident(cx.names.fromString(ConstPool.SN_PKG)),
            cx.names.fromString(ConstPool.SN_CLS), null);
        TestableFieldAccess snMethod = new TestableFieldAccess(snClass,
            cx.names.fromString(ConstPool.SN_METHOD), null);
        JCTree.JCExpression classType = new TestableFieldAccess(cx.treeMaker.Ident(className),
            cx.names.fromString("class"), null);
        ListBuffer<JCTree.JCExpression> args = ListBuffer.of(classType);
        args.addAll(newClassExpr.args);
        return new TestableMethodInvocation(null, snMethod, args.toList());
    }
}
