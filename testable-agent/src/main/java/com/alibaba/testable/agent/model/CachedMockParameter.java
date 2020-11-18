package com.alibaba.testable.agent.model;

import org.objectweb.asm.tree.AnnotationNode;

/**
 * Record parameter fetch from @MockWith annotation
 *
 * @author flin
 */
public class CachedMockParameter {

    private final boolean classExist;
    private final AnnotationNode mockWith;

    private CachedMockParameter(boolean classExist, AnnotationNode mockWith) {
        this.classExist = classExist;
        this.mockWith = mockWith;
    }

    public static CachedMockParameter notExist() {
        return new CachedMockParameter(false, null);
    }

    public static CachedMockParameter exist() {
        return new CachedMockParameter(true, null);
    }

    public static CachedMockParameter exist(AnnotationNode mockWith) {
        return new CachedMockParameter(true, mockWith);
    }

    public boolean isClassExist() {
        return classExist;
    }

    public AnnotationNode getMockWith() {
        return mockWith;
    }
}
