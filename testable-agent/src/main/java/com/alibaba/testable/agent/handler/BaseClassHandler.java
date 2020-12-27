package com.alibaba.testable.agent.handler;

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

    protected static final String TESTABLE_MARK_FIELD = "__testable";

    protected boolean wasTransformed(ClassNode cn) {
        Iterator<FieldNode> iterator = cn.fields.iterator();
        if (iterator.hasNext()) {
            if (TESTABLE_MARK_FIELD.equals(iterator.next().name)) {
                // avoid duplicate injection
                LogUtil.verbose("Duplicate injection found, ignore " + cn.name);
                return true;
            }
        }
        cn.fields.add(new FieldNode(ACC_PRIVATE, TESTABLE_MARK_FIELD, "I", null, null));
        return false;
    }

    public byte[] getBytes(byte[] classFileBuffer) throws IOException {
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
