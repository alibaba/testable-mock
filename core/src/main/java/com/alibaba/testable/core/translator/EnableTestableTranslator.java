package com.alibaba.testable.core.translator;

import com.alibaba.testable.core.constant.ConstPool;
import com.alibaba.testable.core.generator.PrivateAccessStatementGenerator;
import com.alibaba.testable.core.model.TestableContext;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Travel AST
 *
 * @author flin
 */
public class EnableTestableTranslator extends BaseTranslator {

    private final String sourceClassName;
    private final ListBuffer<Name> sourceClassIns = new ListBuffer<>();
    private final ListBuffer<String> privateOrFinalFields = new ListBuffer<>();
    private final ListBuffer<String> privateMethods = new ListBuffer<>();
    private final PrivateAccessStatementGenerator privateAccessStatementGenerator;

    public EnableTestableTranslator(String pkgName, String testClassName, TestableContext cx) {
        this.sourceClassName = testClassName.substring(0, testClassName.length() - ConstPool.TEST_POSTFIX.length());
        this.privateAccessStatementGenerator = new PrivateAccessStatementGenerator(cx);
        try {
            Class<?> cls = Class.forName(pkgName + "." + sourceClassName);
            Field[] fields = cls.getDeclaredFields();
            for (Field f : fields) {
                if (Modifier.isFinal(f.getModifiers()) || Modifier.isPrivate(f.getModifiers())) {
                    privateOrFinalFields.add(f.getName());
                }
            }
            Method[] methods = cls.getDeclaredMethods();
            for (Method m : methods) {
                if (Modifier.isPrivate(m.getModifiers())) {
                    privateMethods.add(m.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void visitVarDef(JCVariableDecl jcVariableDecl) {
        super.visitVarDef(jcVariableDecl);
        if (jcVariableDecl.vartype.getClass().equals(JCIdent.class) &&
            ((JCIdent)jcVariableDecl.vartype).name.toString().equals(sourceClassName)) {
            sourceClassIns.add(jcVariableDecl.name);
        }
    }

    /**
     * d.privateField = val -> PrivateAccessor.set(d, "privateField", val)
     * d.privateMethod(args) -> PrivateAccessor.invoke(d, "privateMethod", args)
     */
    @Override
    public void visitExec(JCExpressionStatement jcExpressionStatement) {
        if (jcExpressionStatement.expr.getClass().equals(JCAssign.class) &&
            isPrivateField((JCAssign)jcExpressionStatement.expr)) {
            jcExpressionStatement.expr = privateAccessStatementGenerator.fetchSetterStatement(
                (JCAssign)jcExpressionStatement.expr);
        }
        jcExpressionStatement.expr = checkAndExchange(jcExpressionStatement.expr);
        super.visitExec(jcExpressionStatement);
    }

    /**
     * For private invoke invocation break point
     */
    @Override
    public void visitApply(JCMethodInvocation tree) {
        tree.args = checkAndExchange(tree.args);
        super.visitApply(tree);
    }

    /**
     * For private setter break point
     */
    @Override
    public void visitAssign(JCAssign jcAssign) {
        super.visitAssign(jcAssign);
    }

    /**
     * For private getter break point
     */
    @Override
    public void visitSelect(JCFieldAccess jcFieldAccess) {
        super.visitSelect(jcFieldAccess);
    }

    @Override
    protected JCExpression checkAndExchange(JCExpression expr) {
        if (expr.getClass().equals(JCMethodInvocation.class) &&
            isPrivateMethod((JCMethodInvocation)expr)) {
            expr = privateAccessStatementGenerator.fetchInvokeStatement((JCMethodInvocation)expr);
        }
        return expr;
    }

    private boolean isPrivateField(JCAssign expr) {
        return expr.lhs.getClass().equals(JCFieldAccess.class) &&
            ((JCFieldAccess)(expr).lhs).selected.getClass().equals(JCIdent.class) &&
            sourceClassIns.contains(((JCIdent)((JCFieldAccess)(expr).lhs).selected).name) &&
            privateOrFinalFields.contains(((JCFieldAccess)(expr).lhs).name.toString());
    }

    private boolean isPrivateMethod(JCMethodInvocation expr) {
        return expr.meth.getClass().equals(JCFieldAccess.class) &&
            ((JCFieldAccess)(expr).meth).selected.getClass().equals(JCIdent.class) &&
            sourceClassIns.contains(((JCIdent)((JCFieldAccess)(expr).meth).selected).name) &&
            privateMethods.contains(((JCFieldAccess)(expr).meth).name.toString());
    }

}
