package com.alibaba.testable.translator;

import com.alibaba.testable.model.TestableContext;
import com.alibaba.testable.util.ConstPool;
import com.sun.tools.javac.tree.JCTree.*;
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
    private List<JCMethodDecl> methods;

    public EnableTestableInjectTranslator(TestableContext cx, List<JCMethodDecl> methods) {
        this.cx = cx;
        this.methods = methods;
    }

    /**
     * new Demo() -> n.e.w(Demo.class)
     * member() -> n.e.f(this, "member")
     */
    @Override
    public void visitExec(JCExpressionStatement jcExpressionStatement) {
        jcExpressionStatement.expr = checkAndExchange(jcExpressionStatement.expr);
        super.visitExec(jcExpressionStatement);
    }

    /**
     * return new Demo() -> return n.e.w(Demo.class)
     * return member() -> return n.e.f(this, "member")
     */
    @Override
    public void visitReturn(JCReturn jcReturn) {
        jcReturn.expr = checkAndExchange(jcReturn.expr);
        super.visitReturn(jcReturn);
    }

    /**
     * Demo d = new Demo() -> Demo d = n.e.w(Demo.class)
     * Demo d = member() -> Demo d = n.e.f(this, "member")
     */
    @Override
    public void visitVarDef(JCVariableDecl jcVariableDecl) {
        jcVariableDecl.init = checkAndExchange(jcVariableDecl.init);
        super.visitVarDef(jcVariableDecl);
    }

    /**
     * new Demo().call() -> n.e.w(Demo.class).call()
     * member().call() -> n.e.f(this, "member").call()
     */
    @Override
    public void visitSelect(JCFieldAccess jcFieldAccess) {
        jcFieldAccess.selected = checkAndExchange(jcFieldAccess.selected);
        super.visitSelect(jcFieldAccess);
    }

    /**
     * For member method invocation break point
     * call(new Demo()) -> call(n.e.w(Demo.class))
     */
    @Override
    public void visitApply(JCMethodInvocation tree) {
        tree.args = checkAndExchange(tree.args);
        super.visitApply(tree);
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

    private JCExpression getGlobalNewInvocation(JCNewClass newClassExpr, Name className) {
        JCFieldAccess snClass = cx.treeMaker.Select(cx.treeMaker.Ident(cx.names.fromString(ConstPool.NE_PKG)),
            cx.names.fromString(ConstPool.NE_CLS));
        JCFieldAccess snMethod = cx.treeMaker.Select(snClass, cx.names.fromString(ConstPool.NE_NEW));
        JCExpression classType = cx.treeMaker.Select(cx.treeMaker.Ident(className),
            cx.names.fromString(ConstPool.TYPE_TO_CLASS));
        ListBuffer<JCExpression> args = ListBuffer.of(classType);
        args.addAll(newClassExpr.args);
        return cx.treeMaker.Apply(List.<JCExpression>nil(), snMethod, args.toList());
    }

    private JCExpression getGlobalMemberInvocation(Name methodName, List<JCExpression> param) {
        JCFieldAccess snClass = cx.treeMaker.Select(cx.treeMaker.Ident(cx.names.fromString(ConstPool.NE_PKG)),
            cx.names.fromString(ConstPool.NE_CLS));
        JCFieldAccess snMethod = cx.treeMaker.Select(snClass, cx.names.fromString(ConstPool.NE_FUN));
        ListBuffer<JCExpression> args = new ListBuffer<>();
        args.add(cx.treeMaker.Ident(cx.names.fromString(ConstPool.REF_THIS)));
        args.add(cx.treeMaker.Literal(methodName.toString()));
        args.addAll(param);
        JCMethodInvocation apply = cx.treeMaker.Apply(List.<JCExpression>nil(), snMethod, args.toList());
        for (JCMethodDecl m : methods) {
            if (m.restype != null && !m.restype.toString().equals(ConstPool.VOID) &&
                m.name.equals(methodName) && paramEquals(m.params, param)) {
                JCTypeCast cast = cx.treeMaker.TypeCast(m.restype, apply);
                return cx.treeMaker.Parens(cast);
            }
        }
        return apply;
    }

    private boolean paramEquals(List<JCVariableDecl> p1, List<JCExpression> p2) {
        if (p1.length() != p2.length()) {
            return false;
        }
        for (int i = 0; i < p1.length(); i++) {
            // TODO: Compare parameters type
        }
        return true;
    }
}
