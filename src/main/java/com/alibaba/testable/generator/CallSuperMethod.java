package com.alibaba.testable.generator;

import com.alibaba.testable.util.ConstPool;
import com.alibaba.testable.util.StringUtil;
import com.sun.tools.javac.tree.JCTree;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Generate call super method statement
 *
 * @author flin
 */
public class CallSuperMethod {
    private final String className;
    private final JCTree.JCMethodDecl method;
    private Object[] params;
    private String statement;

    public CallSuperMethod(String className, JCTree.JCMethodDecl method) {
        this.className = className;
        this.method = method;
    }

    public Object[] getParams() {
        return params;
    }

    public String getStatement() {
        return statement;
    }

    public CallSuperMethod invoke() {
        List<Object> args = new ArrayList<>();
        StringBuilder code = new StringBuilder();
        if (method.getModifiers().getFlags().contains(Modifier.PRIVATE)) {
            reflectCall(args, code);
        } else {
            commonCall(args, code);
        }
        statement = code.toString();
        params = args.toArray();
        return this;
    }

    private void commonCall(List<Object> args, StringBuilder code) {
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
    }

    private void reflectCall(List<Object> args, StringBuilder code) {
        if (!method.restype.toString().equals(ConstPool.CONSTRUCTOR_VOID)) {
            code.append("(").append(method.restype).append(")");
        }
        code.append(className).append(".class.getMethod(\"").append(method.name).append("\"");
        for (JCTree.JCVariableDecl p : method.params) {
            code.append(", $T.class");
            args.add(p.sym.type);
        }
        code.append(").invoke(this");
        for (JCTree.JCVariableDecl p : method.params) {
            code.append(", ").append(p.name);
        }
        code.append(")");
    }
}
