package com.alibaba.testable.generator.statement;

import com.alibaba.testable.generator.model.Statement;
import com.sun.tools.javac.tree.JCTree;

/**
 * @author flin
 */
public interface FieldStatementGenerator {
    /**
     * Generate field access code statement
     */
    Statement[] fetch(String className, JCTree.JCVariableDecl field);
}
