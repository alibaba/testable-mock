package com.alibaba.testable.translator;

import com.alibaba.testable.util.ConstPool;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Name;

import java.util.ArrayList;
import java.util.List;

/**
 * Travel AST
 *
 * @author flin
 */
public class TestableClassTestRoleTranslator extends TreeTranslator {

    private TreeMaker treeMaker;
    private String sourceClassName;
    private List<Name> sourceClassIns = new ArrayList<>();

    public TestableClassTestRoleTranslator(String className, TreeMaker treeMaker) {
        this.sourceClassName = className;
        this.treeMaker = treeMaker;
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

    private JCTree.JCIdent getTestableClassIdent(JCTree.JCExpression clazz) {
        Name className = ((JCTree.JCIdent)clazz).name;
        return treeMaker.Ident(className.table.fromString(className + ConstPool.TESTABLE));
    }

}
