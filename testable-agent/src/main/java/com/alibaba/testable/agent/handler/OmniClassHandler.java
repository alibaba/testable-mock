package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.util.ClassUtil;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import static com.alibaba.testable.core.constant.ConstPool.CONSTRUCTOR;

/**
 * @author flin
 */
public class OmniClassHandler extends BaseClassHandler {

    private static final String TYPE_NULL = "com.alibaba.testable.core.model.Null";

    @Override
    protected void transform(ClassNode cn) {
        if ((cn.access & (ACC_ABSTRACT | ACC_SUPER)) == 0) {
            cn.methods.add(new MethodNode(ACC_PRIVATE, CONSTRUCTOR,
                "(" + ClassUtil.toByteCodeClassName(TYPE_NULL) + ")V", null, null));
        }
    }

}
