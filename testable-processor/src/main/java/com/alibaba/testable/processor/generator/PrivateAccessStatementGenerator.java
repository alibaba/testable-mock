package com.alibaba.testable.processor.generator;

import com.alibaba.testable.processor.model.TestableContext;
import com.alibaba.testable.processor.constant.ConstPool;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

/**
 * @author flin
 */
public class PrivateAccessStatementGenerator extends BaseGenerator {

    public PrivateAccessStatementGenerator(TestableContext cx) {
        super(cx);
    }

    public JCExpression fetchSetterStatement(JCAssign assign) {
        JCFieldAccess setter = cx.treeMaker.Select(nameToExpression(ConstPool.TESTABLE_PRIVATE_ACCESSOR),
            cx.names.fromString("set"));
        return cx.treeMaker.Apply(List.<JCExpression>nil(), setter, List.of(((JCFieldAccess)assign.lhs).selected,
            cx.treeMaker.Literal(((JCFieldAccess)assign.lhs).name.toString()), assign.rhs));
    }

    public JCExpression fetchInvokeStatement(JCMethodInvocation expr) {
        JCFieldAccess invoker = cx.treeMaker.Select(nameToExpression(ConstPool.TESTABLE_PRIVATE_ACCESSOR),
            cx.names.fromString("invoke"));
        ListBuffer<JCExpression> params = ListBuffer.of(((JCFieldAccess)expr.meth).selected);
        params.add(cx.treeMaker.Literal(((JCFieldAccess)expr.meth).name.toString()));
        params.addAll(expr.args);
        return cx.treeMaker.Apply(List.<JCExpression>nil(), invoker, params.toList());
    }
}
