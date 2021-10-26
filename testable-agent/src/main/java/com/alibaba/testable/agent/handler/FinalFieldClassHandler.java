package com.alibaba.testable.agent.handler;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

/**
 * @author flin
 */
public class FinalFieldClassHandler extends BaseClassHandler {

    @Override
    protected void transform(ClassNode cn) {
        if ((cn.access & ACC_INTERFACE) == 0) {
            for (FieldNode field : cn.fields) {
                field.access &= ~ACC_FINAL;
            }
        }
    }

}
