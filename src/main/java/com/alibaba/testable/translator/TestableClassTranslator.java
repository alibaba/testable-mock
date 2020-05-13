package com.alibaba.testable.translator;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;

import java.lang.reflect.Modifier;

/**
 * Travel AST
 *
 * @author flin
 */
public class TestableClassTranslator extends TreeTranslator {

    /**
     * Methods to inject
     */
    private List<JCMethodDecl> methods = List.nil();

    public List<JCMethodDecl> getMethods() {
        return methods;
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

}
