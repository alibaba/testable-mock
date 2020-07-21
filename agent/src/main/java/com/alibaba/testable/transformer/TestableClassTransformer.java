package com.alibaba.testable.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;

/**
 * @author flin
 */
public class TestableClassTransformer implements Opcodes {

    private final ClassNode cn = new ClassNode();

    public TestableClassTransformer(String className) throws IOException {
        ClassReader cr = new ClassReader(className);
        cr.accept(cn, 0);
    }

    public byte[] getBytes() {
        transform();
        ClassWriter cw = new ClassWriter( 0);
        cn.accept(cw);
        return cw.toByteArray();
    }

    private void transform() {

    }

}
