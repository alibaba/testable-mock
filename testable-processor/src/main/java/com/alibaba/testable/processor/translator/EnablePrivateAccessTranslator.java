package com.alibaba.testable.processor.translator;

import com.alibaba.testable.processor.constant.ConstPool;
import com.alibaba.testable.processor.generator.PrivateAccessStatementGenerator;
import com.alibaba.testable.processor.model.TestableContext;
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
public class EnablePrivateAccessTranslator extends BaseTranslator {

    /**
     * Name of source class
     */
    private final String sourceClassName;
    /**
     * Fields of source class instance in the test class
     */
    private final ListBuffer<Name> sourceClassIns = new ListBuffer<Name>();
    /**
     * Record private and final fields
     */
    private final ListBuffer<String> privateOrFinalFields = new ListBuffer<String>();
    /**
     * Record private methods
     */
    private final ListBuffer<String> privateMethods = new ListBuffer<String>();

    private final PrivateAccessStatementGenerator privateAccessStatementGenerator;

    public EnablePrivateAccessTranslator(String pkgName, String testClassName, TestableContext cx) {
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
     * d.privateField = val → PrivateAccessor.set(d, "privateField", val)
     * d.privateMethod(args) → PrivateAccessor.invoke(d, "privateMethod", args)
     */
    @Override
    public void visitExec(JCExpressionStatement jcExpressionStatement) {
        // visitExec could be an assign statement to a private field
        if (jcExpressionStatement.expr.getClass().equals(JCAssign.class) &&
            isPrivateField((JCAssign)jcExpressionStatement.expr)) {
            jcExpressionStatement.expr = privateAccessStatementGenerator.fetchSetterStatement(
                (JCAssign)jcExpressionStatement.expr);
        }
        // visitExec could be an invoke
        jcExpressionStatement.expr = checkAndExchange(jcExpressionStatement.expr);
        super.visitExec(jcExpressionStatement);
    }

    /**
     * For private invoke invocation break point
     * call(d.privateMethod(args)) → call(PrivateAccessor.invoke(d, "privateMethod", args))
     */
    @Override
    public void visitApply(JCMethodInvocation tree) {
        // parameter of invocation could be an invoke or field access
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
        // check is accessing a private field of source class
        if (expr.getClass().equals(JCFieldAccess.class) &&
            isPrivateField((JCFieldAccess)expr)) {
            expr = privateAccessStatementGenerator.fetchGetterStatement((JCFieldAccess)expr);
        }
        // check is invoking a private method of source class
        if (expr.getClass().equals(JCMethodInvocation.class) &&
            isPrivateMethod((JCMethodInvocation)expr)) {
            expr = privateAccessStatementGenerator.fetchInvokeStatement((JCMethodInvocation)expr);
        }
        return expr;
    }

    private boolean isPrivateField(JCFieldAccess access) {
        return access.selected.getClass().equals(JCIdent.class) &&
            sourceClassIns.contains(((JCIdent)access.selected).name) &&
            privateOrFinalFields.contains(access.name.toString());
    }

    private boolean isPrivateField(JCAssign assign) {
        return assign.lhs.getClass().equals(JCFieldAccess.class) &&
            ((JCFieldAccess)(assign).lhs).selected.getClass().equals(JCIdent.class) &&
            sourceClassIns.contains(((JCIdent)((JCFieldAccess)(assign).lhs).selected).name) &&
            privateOrFinalFields.contains(((JCFieldAccess)(assign).lhs).name.toString());
    }

    private boolean isPrivateMethod(JCMethodInvocation expr) {
        return expr.meth.getClass().equals(JCFieldAccess.class) &&
            ((JCFieldAccess)(expr).meth).selected.getClass().equals(JCIdent.class) &&
            sourceClassIns.contains(((JCIdent)((JCFieldAccess)(expr).meth).selected).name) &&
            privateMethods.contains(((JCFieldAccess)(expr).meth).name.toString());
    }

}
