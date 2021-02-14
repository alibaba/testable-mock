package com.alibaba.testable.agent.handler.test;


import com.alibaba.testable.agent.model.TestCaseMethodType;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Set;

abstract public class Framework {

    public boolean hasTestAfterMethod;

    /**
     * Check whether the test class using current test framework
     * @param classAnnotations annotations of the class
     * @param methodAnnotations annotations of all methods
     * @return fit or not
     */
    public boolean fit(Set<String> classAnnotations, Set<String> methodAnnotations) {
        if (methodAnnotations.contains(getTestAfterAnnotation())) {
            hasTestAfterMethod = true;
            return true;
        } else {
            return methodAnnotations.contains(getTestAnnotation());
        }
    }

    public TestCaseMethodType checkMethodType(MethodNode mn) {
        if (mn.visibleAnnotations == null) {
            return TestCaseMethodType.OTHERS;
        }
        for (AnnotationNode an : mn.visibleAnnotations) {
            if (an.desc.equals(getTestAnnotation())) {
                return TestCaseMethodType.TEST;
            } else if (an.desc.equals(getTestAfterAnnotation())) {
                return TestCaseMethodType.AFTER_TEST;
            }
        }
        return TestCaseMethodType.OTHERS;
    }

    public abstract String getTestAnnotation();

    public abstract String getTestAfterAnnotation();

}
