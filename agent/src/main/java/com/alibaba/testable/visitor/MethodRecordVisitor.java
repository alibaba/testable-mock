package com.alibaba.testable.visitor;

import com.alibaba.testable.model.MethodInfo;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.List;

public class MethodRecordVisitor extends ClassVisitor {

    /**
     * Member methods
     */
    private List<MethodInfo> methods = new ArrayList<MethodInfo>();
    private boolean needTransform;

    private static final String ENABLE_TESTABLE_INJECT = "Lcom/alibaba/testable/annotation/EnableTestableInject;";

    public List<MethodInfo> getMethods() {
        return methods;
    }

    public boolean isNeedTransform() {
        return needTransform;
    }

    public MethodRecordVisitor(ClassWriter cw, boolean needTransform) {
        super(Opcodes.ASM8, cw);
        this.needTransform = needTransform;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (descriptor.equals(ENABLE_TESTABLE_INJECT)) {
            needTransform = true;
        }
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        methods.add(new MethodInfo(access, name, desc, signature, exceptions));
        return super.visitMethod(access, name, desc, signature, exceptions);
    }
}
