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

    public JCExpression fetchGetterStatement(JCFieldAccess access) {
        JCFieldAccess getter = cx.treeMaker.Select(nameToExpression(ConstPool.TESTABLE_PRIVATE_ACCESSOR),
            cx.names.fromString("get"));
        return cx.treeMaker.Apply(List.<JCExpression>nil(), getter, List.of(access.selected,
            cx.treeMaker.Literal(access.name.toString())));
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

    public JCExpression fetchStaticGetterStatement(JCFieldAccess access) {
        JCFieldAccess getter = cx.treeMaker.Select(nameToExpression(ConstPool.TESTABLE_PRIVATE_ACCESSOR),
            cx.names.fromString("getStatic"));
        JCExpression classField = cx.treeMaker.Select(access.selected, cx.names.fromString("class"));
        return cx.treeMaker.Apply(List.<JCExpression>nil(), getter, List.of(classField,
            cx.treeMaker.Literal(access.name.toString())));
    }

    public JCExpression fetchStaticSetterStatement(JCAssign assign) {
        JCFieldAccess setter = cx.treeMaker.Select(nameToExpression(ConstPool.TESTABLE_PRIVATE_ACCESSOR),
            cx.names.fromString("setStatic"));
        JCExpression selected = ((JCFieldAccess)assign.lhs).selected;
        JCExpression classField = cx.treeMaker.Select(selected, cx.names.fromString("class"));
        return cx.treeMaker.Apply(List.<JCExpression>nil(), setter, List.of(classField,
            cx.treeMaker.Literal(((JCFieldAccess)assign.lhs).name.toString()), assign.rhs));
    }

    public JCExpression fetchStaticInvokeStatement(JCMethodInvocation expr) {
        JCFieldAccess invoker = cx.treeMaker.Select(nameToExpression(ConstPool.TESTABLE_PRIVATE_ACCESSOR),
            cx.names.fromString("invokeStatic"));
        JCExpression selected = ((JCFieldAccess)expr.meth).selected;
        JCExpression classField = cx.treeMaker.Select(selected, cx.names.fromString("class"));
        ListBuffer<JCExpression> params = ListBuffer.of(classField);
        params.add(cx.treeMaker.Literal(((JCFieldAccess)expr.meth).name.toString()));
        params.addAll(expr.args);
        return cx.treeMaker.Apply(List.<JCExpression>nil(), invoker, params.toList());
    }
}
