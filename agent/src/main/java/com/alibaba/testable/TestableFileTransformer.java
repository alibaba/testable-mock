package com.alibaba.testable;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class TestableFileTransformer implements ClassFileTransformer {

    private static String targetClass = "com/alibaba/testable/TransClass";

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer)
        throws IllegalClassFormatException {
        if (targetClass.equals(className)) {
            // Print class structure with ASM
            ClassReader reader = new ClassReader(classfileBuffer);
            ClassWriter writer = new ClassWriter(reader, 0);
            ClassPrinter visitor = new ClassPrinter(writer);
            reader.accept(visitor, 0);
            return writer.toByteArray();
        }
        return null;
    }

}
