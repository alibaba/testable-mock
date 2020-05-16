package com.alibaba.testable.generator.statement;

import com.alibaba.testable.generator.model.Statement;
import com.alibaba.testable.util.ConstPool;
import com.alibaba.testable.util.StringUtil;
import com.sun.tools.javac.tree.JCTree;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Generate field setter method statement
 *
 * @author flin
 */
public class FieldSetterStatementGenerator implements FieldStatementGenerator {

    @Override
    public Statement[] fetch(String className, JCTree.JCVariableDecl field) {
        return new Statement[]{};
    }

}
