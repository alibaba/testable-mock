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
public class TestableClassTranslator extends TreeTranslator {

    private final TreeMaker treeMaker;

    /**
     * Methods to inject
     */
    private List<JCMethodDecl> methods = List.nil();

    public List<JCMethodDecl> getMethods() {
        return methods;
    }

    public TestableClassTranslator(TreeMaker treeMaker) {
        this.treeMaker = treeMaker;
    }

    @Override
    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
        super.visitClassDef(jcClassDecl);
        jcClassDecl.mods.flags = jcClassDecl.mods.flags & (~Modifier.FINAL);
    }

    @Override
    public void visitMethodDef(JCMethodDecl jcMethodDecl) {
        super.visitMethodDef(jcMethodDecl);
        methods = methods.append(jcMethodDecl);
    }

    @Override
    public void visitExec(JCTree.JCExpressionStatement jcExpressionStatement) {
        if (jcExpressionStatement.expr.getClass().equals(JCTree.JCNewClass.class)) {
            JCTree.JCNewClass newClassExpr = (JCTree.JCNewClass)jcExpressionStatement.expr;
            Name className = ((JCTree.JCIdent)newClassExpr.clazz).name;
            Name.Table nameTable = className.table;
            try {
                JCTree.JCExpression classType = new TestableFieldAccess(treeMaker.Ident(className),
                    nameTable.fromString("class"), null);
                ListBuffer<JCTree.JCExpression> args = ListBuffer.of(classType);
                args.addAll(newClassExpr.args);
                jcExpressionStatement.expr = new TestableMethodInvocation(null,
                    new TestableFieldAccess(treeMaker.Ident(nameTable.fromString(ConstPool.SN_PKG_CLS)),
                        nameTable.fromString(ConstPool.SN_METHOD), null), args.toList());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.visitExec(jcExpressionStatement);
    }

    @Override
    public void visitApply(JCTree.JCMethodInvocation tree) {
        super.visitApply(tree);
    }

    @Override
    public void visitNewClass(JCTree.JCNewClass jcNewClass) {
        super.visitNewClass(jcNewClass);
    }
}
