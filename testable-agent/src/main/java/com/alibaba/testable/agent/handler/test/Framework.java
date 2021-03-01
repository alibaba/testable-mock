package com.alibaba.testable.agent.handler.test;

import com.alibaba.testable.agent.model.TestCaseMethodType;
import com.alibaba.testable.agent.util.CollectionUtil;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;
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
            return CollectionUtil.containsAny(methodAnnotations, getTestAnnotations());
        }
    }

    public TestCaseMethodType checkMethodType(MethodNode mn) {
        if (mn.visibleAnnotations == null) {
            return TestCaseMethodType.OTHERS;
        }
        for (AnnotationNode an : mn.visibleAnnotations) {
            if (getTestAnnotations().contains(an.desc)) {
                return TestCaseMethodType.TEST;
            } else if (an.desc.equals(getTestAfterAnnotation())) {
                return TestCaseMethodType.AFTER_TEST;
            }
        }
        return TestCaseMethodType.OTHERS;
    }

    public abstract List<String> getTestAnnotations();

    public abstract String getTestAfterAnnotation();

}
