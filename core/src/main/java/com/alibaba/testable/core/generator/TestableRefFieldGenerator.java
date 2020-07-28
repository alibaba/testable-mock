package com.alibaba.testable.core.generator;

import com.alibaba.testable.core.constant.ConstPool;
import com.alibaba.testable.core.model.TestableContext;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import java.lang.reflect.Modifier;

/**
 * Generate test class reference field
 *
 * @author flin
 */
public class TestableRefFieldGenerator extends BaseGenerator {

    private final String testClassFullName;

    public TestableRefFieldGenerator(TestableContext cx, String testClassFullName) {
        super(cx);
        this.testClassFullName = testClassFullName;
    }

    public JCVariableDecl fetch() {
        JCModifiers mods = cx.treeMaker.Modifiers(Modifier.PUBLIC | Modifier.STATIC);
        return cx.treeMaker.VarDef(mods, cx.names.fromString(ConstPool.TESTABLE_REF_FIELD_NAME),
            nameToExpression(testClassFullName), null);
    }

}
