package com.alibaba.testable.agent.handler;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

/**
 * @author flin
 */
abstract public class BaseClassHandler implements Opcodes {

    protected static final String GET_TESTABLE_REF = "testableIns";
    protected static final String VOID_ARGS = "()";
    protected static final String VOID_RES = "V";

    public byte[] getBytes(byte[] classFileBuffer) {
        ClassReader cr = new ClassReader(classFileBuffer);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);
        transform(cn);
        ClassWriter cw = new ClassWriter( 0);
        cn.accept(cw);
        return cw.toByteArray();
    }

    /**
     * Transform class byte code
     * @param cn original class node
     */
    abstract protected void transform(ClassNode cn);

}
