package com.alibaba.testable.generator;

import com.alibaba.testable.generator.model.Statement;
import com.alibaba.testable.util.ConstPool;
import com.alibaba.testable.util.StringUtil;
import com.sun.tools.javac.tree.JCTree;
import java.lang.reflect.Method;
import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Generate call super method statement
 *
 * @author flin
 */
public class CallSuperMethodStatementGenerator {

    public Statement[] fetch(String className, JCTree.JCMethodDecl method) {
        if (method.getModifiers().getFlags().contains(Modifier.PRIVATE)) {
            return reflectCall(className, method);
        } else {
            return commonCall(method);
        }
    }

    private Statement[] commonCall(JCTree.JCMethodDecl method) {
        List<Object> args = new ArrayList<>();
        StringBuilder code = new StringBuilder();
        List<String> placeholders = new ArrayList<>();
        for (JCTree.JCVariableDecl p : method.params) {
            args.add(p.name.toString());
            placeholders.add("$N");
        }
        code.append("super");
        if (!method.name.toString().equals(ConstPool.CONSTRUCTOR_NAME)) {
            code.append(".").append(method.name);
        }
        code.append("(").append(StringUtil.join(placeholders, ", ")).append(")");
        return new Statement[] { returnStatement(method, new Statement(code.toString(), args.toArray())) };
    }

    private Statement[] reflectCall(String className, JCTree.JCMethodDecl method) {
        List<Statement> statements = new ArrayList<>();
        statements.add(getMethodStatement(className, method));
        statements.add(setAccessibleStatement());
        statements.add(returnStatement(method, invokeStatement(method)));
        return statements.toArray(new Statement[0]);
    }

    private Statement returnStatement(JCTree.JCMethodDecl method, Statement statement) {
        if (method.restype != null && !method.restype.toString().equals(ConstPool.TYPE_VOID)) {
            statement.setLine("return " + statement.getLine());
        }
        return statement;
    }

    private Statement getMethodStatement(String className, JCTree.JCMethodDecl method) {
        List<Object> args = new ArrayList<>();
        StringBuilder code = new StringBuilder();
        code.append("$T m = ");
        args.add(Method.class);
        code.append(className).append(".class.getDeclaredMethod(\"").append(method.name).append("\"");
        for (JCTree.JCVariableDecl p : method.params) {
            code.append(", $T.class");
            args.add(p.sym.type);
        }
        code.append(")");
        return new Statement(code.toString(), args.toArray());
    }

    private Statement setAccessibleStatement() {
        return new Statement("m.setAccessible(true)", new Object[0]);
    }

    private Statement invokeStatement(JCTree.JCMethodDecl method) {
        StringBuilder code = new StringBuilder();
        if (!method.restype.toString().equals(ConstPool.TYPE_VOID)) {
            code.append("(").append(method.restype).append(")");
        }
        code.append("m.invoke(this");
        for (JCTree.JCVariableDecl p : method.params) {
            code.append(", ").append(p.name);
        }
        code.append(")");
        return new Statement(code.toString(), new Object[0]);
    }
}
