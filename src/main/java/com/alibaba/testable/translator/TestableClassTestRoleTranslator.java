package com.alibaba.testable.translator;

import com.alibaba.testable.util.ConstPool;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Name;

import java.util.ArrayList;
import java.util.Arrays;
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
    private List<String> stubbornFields = new ArrayList<>();

    public TestableClassTestRoleTranslator(String pkgName, String className, TreeMaker treeMaker) {
        this.sourceClassName = className;
        this.treeMaker = treeMaker;
        try {
            stubbornFields = Arrays.asList(
                (String[])Class.forName(pkgName + "." + className + ConstPool.TESTABLE)
                .getMethod(ConstPool.STUBBORN_FIELD_METHOD)
                .invoke(null));
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    /**
     * For break point
     */
    @Override
    public void visitAssign(JCTree.JCAssign jcAssign) {
        super.visitAssign(jcAssign);
    }

    /**
     * For break point
     */
    @Override
    public void visitSelect(JCTree.JCFieldAccess jcFieldAccess) {
        super.visitSelect(jcFieldAccess);
    }

    @Override
    public void visitExec(JCTree.JCExpressionStatement jcExpressionStatement) {
        if (jcExpressionStatement.expr.getClass().equals(JCTree.JCAssign.class) &&
            isAssignStubbornField((JCTree.JCAssign)jcExpressionStatement.expr)) {
            //jcExpressionStatement.expr =
        }
        super.visitExec(jcExpressionStatement);
    }

    private boolean isAssignStubbornField(JCTree.JCAssign expr) {
        return expr.lhs.getClass().equals(JCTree.JCFieldAccess.class) &&
            sourceClassIns.contains(((JCTree.JCIdent)((JCTree.JCFieldAccess)(expr).lhs).selected).name) &&
            stubbornFields.contains(((JCTree.JCFieldAccess)(expr).lhs).name.toString());
    }

    private JCTree.JCIdent getTestableClassIdent(JCTree.JCExpression clazz) {
        Name className = ((JCTree.JCIdent)clazz).name;
        return treeMaker.Ident(className.table.fromString(className + ConstPool.TESTABLE));
    }

}
