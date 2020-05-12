package com.alibaba.testable.processor;

import com.alibaba.testable.util.ConstPool;
import com.alibaba.testable.util.StringUtil;
import com.sun.tools.javac.tree.JCTree;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
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
        List<String> placeholders = new ArrayList<>();
        if (method.getModifiers().getFlags().contains(Modifier.PRIVATE)) {
            statement = className + ".class.getMethod(\"" + method.name + "\").invoke(this)";
            if (!method.restype.toString().equals(ConstPool.CONSTRUCTOR_VOID)) {
                statement = "(" + method.restype + ")" + statement;
            }
        } else {
            for (JCTree.JCVariableDecl p : method.params) {
                args.add(p.name.toString());
                placeholders.add("$N");
            }
            String call = "super";
            if (!method.name.toString().equals(ConstPool.CONSTRUCTOR_NAME)) {
                call += ("." + method.name.toString());
            }
            statement = call + "(" + StringUtil.join(placeholders, ", ") + ")";
        }
        params = args.toArray();
        return this;
    }
}
