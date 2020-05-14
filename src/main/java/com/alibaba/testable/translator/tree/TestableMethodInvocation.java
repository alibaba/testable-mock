package com.alibaba.testable.translator.tree;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

/**
 * @author flin
 */
public class TestableMethodInvocation extends JCTree.JCMethodInvocation {

    public TestableMethodInvocation(List<JCExpression> typeargs, JCExpression meth, List<JCExpression> args) {
        super(typeargs, meth, args);
    }

}
