package com.alibaba.testable.translator;

import com.alibaba.testable.model.TestableContext;
import com.alibaba.testable.util.ConstPool;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

/**
 * Travel AST
 *
 * @author flin
 */
public class EnableTestableInjectTranslator extends TreeTranslator {

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
    public void visitExec(JCTree.JCExpressionStatement jcExpressionStatement) {
        jcExpressionStatement.expr = checkAndExchange(jcExpressionStatement.expr);
        super.visitExec(jcExpressionStatement);
    }

    /**
     * For member method invocation break point
     * Case: call(new Demo())
     */
    @Override
    public void visitApply(JCTree.JCMethodInvocation tree) {
        tree.args = checkAndExchange(tree.args);
        super.visitApply(tree);
    }

    /**
     * Case: return new Demo()
     * Case: return member()
     */
    @Override
    public void visitReturn(JCTree.JCReturn jcReturn) {
        jcReturn.expr = checkAndExchange(jcReturn.expr);
        super.visitReturn(jcReturn);
    }

    /**
     * Record all private fields
     * Case: Demo d = new Demo()
     * Case: Demo d = member()
     */
    @Override
    public void visitVarDef(JCTree.JCVariableDecl jcVariableDecl) {
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
    public void visitSelect(JCTree.JCFieldAccess jcFieldAccess) {
        jcFieldAccess.selected = checkAndExchange(jcFieldAccess.selected);
        super.visitSelect(jcFieldAccess);
    }

    /**
     * For new operation break point
     */
    @Override
    public void visitNewClass(JCTree.JCNewClass jcNewClass) {
        super.visitNewClass(jcNewClass);
    }

    /**
     * For new operation break point
     */
    @Override
    public void visitNewArray(JCTree.JCNewArray jcNewArray) {
        super.visitNewArray(jcNewArray);
    }

    private boolean isStubbornField(JCTree.JCModifiers mods) {
        return mods.getFlags().contains(javax.lang.model.element.Modifier.PRIVATE) ||
            mods.getFlags().contains(javax.lang.model.element.Modifier.FINAL);
    }

    private List<JCTree.JCExpression> checkAndExchange(List<JCTree.JCExpression> args) {
        if (args != null) {
            JCTree.JCExpression[] es = new JCTree.JCExpression[args.length()];
            for (int i = 0; i < args.length(); i++) {
                es[i] = checkAndExchange(args.get(i));
            }
            return List.from(es);
        }
        return null;
    }

    private JCTree.JCExpression checkAndExchange(JCTree.JCExpression expr) {
        if (isNewOperation(expr)) {
            JCTree.JCNewClass newClassExpr = (JCTree.JCNewClass)expr;
            Name className = ((JCTree.JCIdent)newClassExpr.clazz).name;
            try {
                return getGlobalNewInvocation(newClassExpr, className);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (isMemberMethodInvocation(expr)) {
            Name methodName = ((JCTree.JCIdent)((JCTree.JCMethodInvocation)expr).meth).name;
            List<JCTree.JCExpression> args = ((JCTree.JCMethodInvocation)expr).args;
            return getGlobalMemberInvocation(methodName, args);
        }
        return expr;
    }

    private boolean isMemberMethodInvocation(JCTree.JCExpression expr) {
        return expr != null && expr.getClass().equals(JCTree.JCMethodInvocation.class) &&
            ((JCTree.JCMethodInvocation)expr).meth.getClass().equals(JCTree.JCIdent.class);
    }

    private boolean isNewOperation(JCTree.JCExpression expr) {
        return expr != null && expr.getClass().equals(JCTree.JCNewClass.class);
    }

    private JCTree.JCMethodInvocation getGlobalNewInvocation(JCTree.JCNewClass newClassExpr, Name className) {
        JCTree.JCFieldAccess snClass = cx.treeMaker.Select(cx.treeMaker.Ident(cx.names.fromString(ConstPool.NE_PKG)),
            cx.names.fromString(ConstPool.NE_CLS));
        JCTree.JCFieldAccess snMethod = cx.treeMaker.Select(snClass, cx.names.fromString(ConstPool.NE_NEW));
        JCTree.JCExpression classType = cx.treeMaker.Select(cx.treeMaker.Ident(className),
            cx.names.fromString(ConstPool.TYPE_TO_CLASS));
        ListBuffer<JCTree.JCExpression> args = ListBuffer.of(classType);
        args.addAll(newClassExpr.args);
        return cx.treeMaker.Apply(List.<JCTree.JCExpression>nil(), snMethod, args.toList());
    }

    private JCTree.JCMethodInvocation getGlobalMemberInvocation(Name methodName, List<JCTree.JCExpression> param) {
        JCTree.JCFieldAccess snClass = cx.treeMaker.Select(cx.treeMaker.Ident(cx.names.fromString(ConstPool.NE_PKG)),
            cx.names.fromString(ConstPool.NE_CLS));
        JCTree.JCFieldAccess snMethod = cx.treeMaker.Select(snClass, cx.names.fromString(ConstPool.NE_FUN));
        ListBuffer<JCTree.JCExpression> args = new ListBuffer();
        args.add(cx.treeMaker.Ident(cx.names.fromString(ConstPool.REF_THIS)));
        args.add(cx.treeMaker.Literal(methodName.toString()));
        args.addAll(param);
        return cx.treeMaker.Apply(List.<JCTree.JCExpression>nil(), snMethod, args.toList());
    }
}
