package com.alibaba.testable.translator;

import com.alibaba.testable.translator.tree.TestableFieldAccess;
import com.alibaba.testable.translator.tree.TestableMethodInvocation;
import com.alibaba.testable.util.ConstPool;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.TreeMaker;
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

    private final TreeMaker treeMaker;

    /**
     * Methods to inject
     */
    private List<JCMethodDecl> methods = List.nil();

    /**
     * Private field to mock
     */
    private List<JCTree.JCVariableDecl> fields = List.nil();

    public List<JCMethodDecl> getMethods() {
        return methods;
    }

    public List<JCTree.JCVariableDecl> getPrivateFields() {
        return fields;
    }

    public TestableClassDevRoleTranslator(TreeMaker treeMaker) {
        this.treeMaker = treeMaker;
    }

    @Override
    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
        super.visitClassDef(jcClassDecl);
        jcClassDecl.mods.flags = jcClassDecl.mods.flags & (~Modifier.FINAL);
    }

    /**
     * record all methods
     */
    @Override
    public void visitMethodDef(JCMethodDecl jcMethodDecl) {
        super.visitMethodDef(jcMethodDecl);
        methods = methods.append(jcMethodDecl);
    }

    /**
     * case: new Demo()
     */
    @Override
    public void visitExec(JCTree.JCExpressionStatement jcExpressionStatement) {
        jcExpressionStatement.expr = checkAndExchangeNewOperation(jcExpressionStatement.expr);
        super.visitExec(jcExpressionStatement);
    }

    /**
     * case: call(new Demo())
     */
    @Override
    public void visitApply(JCTree.JCMethodInvocation tree) {
        tree.args = checkAndExchangeNewOperation(tree.args);
        super.visitApply(tree);
    }

    /**
     * record all private fields
     * case: Demo d = new Demo()
     */
    @Override
    public void visitVarDef(JCTree.JCVariableDecl jcVariableDecl) {
        if (jcVariableDecl.mods.getFlags().contains(javax.lang.model.element.Modifier.PRIVATE)) {
            fields.append(jcVariableDecl);
        }
        jcVariableDecl.init = checkAndExchangeNewOperation(jcVariableDecl.init);
        super.visitVarDef(jcVariableDecl);
    }

    /**
     * case: new Demo().call()
     */
    @Override
    public void visitSelect(JCTree.JCFieldAccess jcFieldAccess) {
        jcFieldAccess.selected = checkAndExchangeNewOperation(jcFieldAccess.selected);
        super.visitSelect(jcFieldAccess);
    }

    @Override
    public void visitNewClass(JCTree.JCNewClass jcNewClass) {
        super.visitNewClass(jcNewClass);
    }

    @Override
    public void visitNewArray(JCTree.JCNewArray jcNewArray) {
        super.visitNewArray(jcNewArray);
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
            Name.Table nameTable = className.table;
            try {
                return getStaticNewCall(newClassExpr, className, nameTable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return expr;
    }

    private boolean isNewOperation(JCTree.JCExpression expr) {
        return expr != null && expr.getClass().equals(JCTree.JCNewClass.class);
    }

    private TestableMethodInvocation getStaticNewCall(JCTree.JCNewClass newClassExpr, Name className,
                                                      Name.Table nameTable) {
        TestableFieldAccess snClass = new TestableFieldAccess(treeMaker.Ident(nameTable.fromString(ConstPool.SN_PKG)),
            nameTable.fromString(ConstPool.SN_CLS), null);
        TestableFieldAccess snMethod = new TestableFieldAccess(snClass,
            nameTable.fromString(ConstPool.SN_METHOD), null);
        JCTree.JCExpression classType = new TestableFieldAccess(treeMaker.Ident(className),
            nameTable.fromString("class"), null);
        ListBuffer<JCTree.JCExpression> args = ListBuffer.of(classType);
        args.addAll(newClassExpr.args);
        return new TestableMethodInvocation(null, snMethod, args.toList());
    }
}
