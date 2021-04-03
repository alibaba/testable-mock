package com.alibaba.testable.agent.handler.test;

import com.alibaba.testable.agent.model.TestCaseMethodType;
import org.objectweb.asm.tree.MethodNode;

import java.util.Set;

/**
 * @author flin
 */
public interface Framework {

    /**
     * Check whether the test class using current test framework
     * @param classAnnotations annotations of the class
     * @param methodAnnotations annotations of all methods
     * @return fit or not
     */
    boolean fit(Set<String> classAnnotations, Set<String> methodAnnotations);

    /**
     * Check whether a method is test or cleanup method
     * @param mn method node
     * @return test method / cleanup method / other method
     */
    TestCaseMethodType checkMethodType(MethodNode mn);

    /**
     * Generate cleanup method with correct name and annotations
     * @param className full name of test class
     * @return cleanup method for current framework
     */
    MethodNode getCleanupMethod(String className);

}
