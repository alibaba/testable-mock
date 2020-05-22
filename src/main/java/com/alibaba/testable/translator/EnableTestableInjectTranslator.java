package com.alibaba.testable.translator;

import com.alibaba.testable.model.TestableContext;
import com.alibaba.testable.util.ConstPool;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

/**
 * Travel AST
 *
 * @author flin
 */
public class EnableTestableInjectTranslator extends BaseTranslator {

    private final TestableContext cx;

    /**
     * Methods to inject
     */
    private List<JCMethodDecl> methods = List.nil();

    /**
     * Fields to wrap
     */
    private List<JCVariableDecl> fields = List.nil();

    public List<JCMethodDecl> getMethods() {
        return methods;
    }

    public List<JCVariableDecl> getFields() {
        return fields;
    }

    public EnableTestableInjectTranslator(TestableContext cx) {
        this.cx = cx;
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
     * Case: member()
     */
    @Override
    public void visitExec(JCExpressionStatement jcExpressionStatement) {
        jcExpressionStatement.expr = checkAndExchange(jcExpressionStatement.expr);
        super.visitExec(jcExpressionStatement);
    }

    /**
     * For member method invocation break point
     * Case: call(new Demo())
     */
    @Override
    public void visitApply(JCMethodInvocation tree) {
        tree.args = checkAndExchange(tree.args);
        super.visitApply(tree);
    }

    /**
     * Case: return new Demo()
     * Case: return member()
     */
    @Override
    public void visitReturn(JCReturn jcReturn) {
        jcReturn.expr = checkAndExchange(jcReturn.expr);
        super.visitReturn(jcReturn);
    }

    /**
     * Record all private fields
     * Case: Demo d = new Demo()
     * Case: Demo d = member()
     */
    @Override
    public void visitVarDef(JCVariableDecl jcVariableDecl) {
        if (isStubbornField(jcVariableDecl.mods)) {
            fields = fields.append(jcVariableDecl);
        }
        jcVariableDecl.init = checkAndExchange(jcVariableDecl.init);
        super.visitVarDef(jcVariableDecl);
    }

    /**
     * Case: new Demo().call()
     * Case: member().call()
     */
    @Override
    public void visitSelect(JCFieldAccess jcFieldAccess) {
        jcFieldAccess.selected = checkAndExchange(jcFieldAccess.selected);
        super.visitSelect(jcFieldAccess);
    }

    /**
     * For new operation break point
     */
    @Override
    public void visitNewClass(JCNewClass jcNewClass) {
        super.visitNewClass(jcNewClass);
    }

    /**
     * For new operation break point
     */
    @Override
    public void visitNewArray(JCNewArray jcNewArray) {
        super.visitNewArray(jcNewArray);
    }

    private boolean isStubbornField(JCModifiers mods) {
        return mods.getFlags().contains(javax.lang.model.element.Modifier.PRIVATE) ||
            mods.getFlags().contains(javax.lang.model.element.Modifier.FINAL);
    }

    @Override
    protected JCExpression checkAndExchange(JCExpression expr) {
        if (isNewOperation(expr)) {
            JCNewClass newClassExpr = (JCNewClass)expr;
            Name className = ((JCIdent)newClassExpr.clazz).name;
            try {
                return getGlobalNewInvocation(newClassExpr, className);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (isMemberMethodInvocation(expr)) {
            Name methodName = ((JCIdent)((JCMethodInvocation)expr).meth).name;
            List<JCExpression> args = ((JCMethodInvocation)expr).args;
            return getGlobalMemberInvocation(methodName, args);
        }
        return expr;
    }

    private boolean isMemberMethodInvocation(JCExpression expr) {
        return expr != null && expr.getClass().equals(JCMethodInvocation.class) &&
            ((JCMethodInvocation)expr).meth.getClass().equals(JCIdent.class);
    }

    private boolean isNewOperation(JCExpression expr) {
        return expr != null && expr.getClass().equals(JCNewClass.class);
    }

    private JCMethodInvocation getGlobalNewInvocation(JCNewClass newClassExpr, Name className) {
        JCFieldAccess snClass = cx.treeMaker.Select(cx.treeMaker.Ident(cx.names.fromString(ConstPool.NE_PKG)),
            cx.names.fromString(ConstPool.NE_CLS));
        JCFieldAccess snMethod = cx.treeMaker.Select(snClass, cx.names.fromString(ConstPool.NE_NEW));
        JCExpression classType = cx.treeMaker.Select(cx.treeMaker.Ident(className),
            cx.names.fromString(ConstPool.TYPE_TO_CLASS));
        ListBuffer<JCExpression> args = ListBuffer.of(classType);
        args.addAll(newClassExpr.args);
        return cx.treeMaker.Apply(List.<JCExpression>nil(), snMethod, args.toList());
    }

    private JCMethodInvocation getGlobalMemberInvocation(Name methodName, List<JCExpression> param) {
        JCFieldAccess snClass = cx.treeMaker.Select(cx.treeMaker.Ident(cx.names.fromString(ConstPool.NE_PKG)),
            cx.names.fromString(ConstPool.NE_CLS));
        JCFieldAccess snMethod = cx.treeMaker.Select(snClass, cx.names.fromString(ConstPool.NE_FUN));
        ListBuffer<JCExpression> args = new ListBuffer<>();
        args.add(cx.treeMaker.Ident(cx.names.fromString(ConstPool.REF_THIS)));
        args.add(cx.treeMaker.Literal(methodName.toString()));
        args.addAll(param);
        return cx.treeMaker.Apply(List.<JCExpression>nil(), snMethod, args.toList());
    }
}
