package com.alibaba.testable.translator.tree;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Name;

/**
 * @author flin
 */
public class TestableFieldAccess extends JCTree.JCFieldAccess {

    public TestableFieldAccess(JCExpression selected, Name name, Symbol sym) {
        super(selected, name, sym);
    }

}
