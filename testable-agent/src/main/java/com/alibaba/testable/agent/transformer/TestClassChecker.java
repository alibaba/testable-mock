package com.alibaba.testable.agent.transformer;

import com.alibaba.testable.agent.handler.test.*;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashSet;
import java.util.Set;

/**
 * @author flin
 */
public class TestClassChecker {

    private final Framework[] frameworkClasses = new Framework[] {
        new JUnit4Framework(),
        new JUnit5Framework(),
        new TestNgFramework(),
        new TestNgOnClassFramework(),
        new SpockFramework()
    };

    public Framework checkFramework(ClassNode cn) {
        Set<String> classAnnotationSet = new HashSet<String>();
        Set<String> methodAnnotationSet = new HashSet<String>();
        if (cn.visibleAnnotations != null) {
            for (AnnotationNode an : cn.visibleAnnotations) {
                classAnnotationSet.add(an.desc);
            }
        }
        for (MethodNode mn : cn.methods) {
            if (mn.visibleAnnotations != null) {
                for (AnnotationNode an : mn.visibleAnnotations) {
                    methodAnnotationSet.add(an.desc);
                }
            }
        }
        for (Framework i : frameworkClasses) {
            if (i.fit(classAnnotationSet, methodAnnotationSet)) {
                return i;
            }
        }
        return null;
    }

}
