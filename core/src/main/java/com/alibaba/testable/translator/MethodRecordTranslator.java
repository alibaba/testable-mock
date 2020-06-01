package com.alibaba.testable.translator;

import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;

/**
 * Travel AST
 *
 * @author flin
 */
public class MethodRecordTranslator extends TreeTranslator {

    /**
     * Member methods
     */
    private List<JCMethodDecl> methods = List.nil();

    public List<JCMethodDecl> getMethods() {
        return methods;
    }

    /**
     * Record all methods
     */
    @Override
    public void visitMethodDef(JCMethodDecl jcMethodDecl) {
        super.visitMethodDef(jcMethodDecl);
        methods = methods.append(jcMethodDecl);
    }

}
