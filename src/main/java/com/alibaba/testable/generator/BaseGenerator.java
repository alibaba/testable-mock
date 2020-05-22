package com.alibaba.testable.generator;

import com.alibaba.testable.model.TestableContext;
import com.sun.tools.javac.tree.JCTree.*;

/**
 * @author flin
 */
public abstract class BaseGenerator {

    protected final TestableContext cx;

    protected BaseGenerator(TestableContext cx) {
        this.cx = cx;
    }

    protected JCExpression nameToExpression(String dotName) {
        String[] nameParts = dotName.split("\\.");
        JCExpression e = cx.treeMaker.Ident(cx.names.fromString(nameParts[0]));
        for (int i = 1; i < nameParts.length; i++) {
            e = cx.treeMaker.Select(e, cx.names.fromString(nameParts[i]));
        }
        return e;
    }

}
