package com.alibaba.testable.util;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.alibaba.testable.constant.Const.*;

/**
 * @author flin
 */
public class ClassUtil {

    public static List<String> getAnnotations(String className) {
        try {
            List<String> annotations = new ArrayList<String>();
            ClassNode cn = new ClassNode();
            new ClassReader(className).accept(cn, 0);
            for (AnnotationNode an : cn.visibleAnnotations) {
                String annotationName = an.desc.replace(SLASH, DOT).substring(1, an.desc.length() - 1);
                annotations.add(annotationName);
            }
            return annotations;
        } catch (IOException e) {
            return null;
        }
    }

}
