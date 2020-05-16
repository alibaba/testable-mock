package com.alibaba.testable.translator;

import com.alibaba.testable.util.ConstPool;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Name;

/**
 * Travel AST
 *
 * @author flin
 */
public class TestableClassTestRoleTranslator extends TreeTranslator {

    private TreeMaker treeMaker;

    public TestableClassTestRoleTranslator(TreeMaker treeMaker) {this.treeMaker = treeMaker;}

    @Override
    public void visitVarDef(JCTree.JCVariableDecl decl) {
        super.visitVarDef(decl);
        decl.vartype = getTestableClassIdent(decl.vartype);
    }

    @Override
    public void visitNewClass(JCTree.JCNewClass jcNewClass) {
        super.visitNewClass(jcNewClass);
        jcNewClass.clazz = getTestableClassIdent(jcNewClass.clazz);
    }

    private JCTree.JCIdent getTestableClassIdent(JCTree.JCExpression clazz) {
        Name className = ((JCTree.JCIdent)clazz).name;
        return treeMaker.Ident(className.table.fromString(className + ConstPool.TESTABLE));
    }

}
