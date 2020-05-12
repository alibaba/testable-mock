package com.alibaba.testable.processor;

import com.alibaba.testable.util.ConstPool;
import com.alibaba.testable.util.StringUtil;
import com.sun.tools.javac.tree.JCTree;

import java.util.ArrayList;
import java.util.List;

/**
 * @author flin
 */
public class CallSuperMethod {
    private final JCTree.JCMethodDecl method;
    private Object[] params;
    private String statement;

    public CallSuperMethod(JCTree.JCMethodDecl method) {this.method = method;}

    public Object[] getParams() {
        return params;
    }

    public String getStatement() {
        return statement;
    }

    public CallSuperMethod invoke() {
        params = new String[method.params.length()];
        List<String> placeholders = new ArrayList<>();
        for (int i = 0; i < method.params.length(); i++) {
            params[i] = (method.params.get(i).name.toString());
            placeholders.add("$N");
        }
        String call = "super";
        if (!method.name.toString().equals(ConstPool.CONSTRUCTOR_NAME)) {
            call += ("." + method.name.toString());
        }
        statement = call + "(" + StringUtil.join(placeholders, ", ") + ")";
        return this;
    }
}
