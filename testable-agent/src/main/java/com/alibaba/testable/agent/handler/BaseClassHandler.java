package com.alibaba.testable.agent.handler;

import com.alibaba.testable.agent.util.ClassUtil;
import com.alibaba.testable.core.util.LogUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author flin
 */
abstract public class BaseClassHandler implements Opcodes {

    protected static final String TESTABLE_REF = "__testable";
    protected static final String GET_TESTABLE_REF = "testableIns";
    protected static final String VOID_ARGS = "()";
    protected static final String VOID_RES = "V";

    protected String mockClassName;

    protected boolean markTransformed(ClassNode cn, String refName, String refDescriptor) {
        Iterator<FieldNode> iterator = cn.fields.iterator();
        if (iterator.hasNext()) {
            if (refName.equals(iterator.next().name)) {
                // avoid duplicate injection
                LogUtil.verbose("Duplicate injection found, ignore " + cn.name);
                return false;
            }
        }
        cn.fields.add(new FieldNode(ACC_PRIVATE | ACC_STATIC, refName, refDescriptor, null, null));
        return true;
    }

    public byte[] getBytes(byte[] classFileBuffer) throws IOException {
        ClassReader cr = new ClassReader(classFileBuffer);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);
        if (markTransformed(cn, TESTABLE_REF, ClassUtil.toByteCodeClassName(mockClassName))) {
            transform(cn);
        }
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
