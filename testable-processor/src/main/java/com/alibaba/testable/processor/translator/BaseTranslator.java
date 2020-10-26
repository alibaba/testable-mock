package com.alibaba.testable.processor.translator;

import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;

/**
 * @author flin
 */
public abstract class BaseTranslator extends TreeTranslator {

    protected List<JCExpression> checkAndExchange(List<JCExpression> args) {
        if (args != null) {
            JCExpression[] es = new JCExpression[args.length()];
            for (int i = 0; i < args.length(); i++) {
                es[i] = checkAndExchange(args.get(i));
            }
            return List.from(es);
        }
        return List.nil();
    }

    /**
     * Exchange private method invoke with private accessor
     * @param expr original expression
     * @return exchanged expression
     */
    protected abstract JCExpression checkAndExchange(JCExpression expr);

}
