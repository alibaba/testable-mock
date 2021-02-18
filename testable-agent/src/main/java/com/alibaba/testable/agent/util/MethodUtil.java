package com.alibaba.testable.agent.util;

import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.ACC_STATIC;

public class MethodUtil {

    public static boolean isStaticMethod(MethodNode mn) {
        return (mn.access & ACC_STATIC) != 0;
    }

}
