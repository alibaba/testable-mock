package com.alibaba.testable.generator;

import com.alibaba.testable.model.TestableContext;
import com.alibaba.testable.util.ConstPool;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.List;

/**
 * @author flin
 */
public class PrivateAccessStatementGenerator extends BaseGenerator {

    public PrivateAccessStatementGenerator(TestableContext cx) {
        super(cx);
    }

    public JCExpression fetchSetterStatement(JCExpressionStatement jcExpressionStatement) {
        JCAssign assign = (JCAssign)jcExpressionStatement.expr;
        JCFieldAccess setter = cx.treeMaker.Select(nameToExpression(ConstPool.TESTABLE_PRIVATE_ACCESSOR),
            cx.names.fromString("set"));
        return cx.treeMaker.Apply(List.<JCExpression>nil(), setter, List.of(((JCFieldAccess)assign.lhs).selected,
            cx.treeMaker.Literal(((JCFieldAccess)assign.lhs).name.toString()), assign.rhs));
    }
}
