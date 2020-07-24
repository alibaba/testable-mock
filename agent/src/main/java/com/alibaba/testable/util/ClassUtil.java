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

    public static int getParameterCount(String paramTypes) {
        int paramCount = 0;
        boolean travelingClass = false;
        for (byte b : paramTypes.getBytes()) {
            if (travelingClass) {
                if (b == ';') {
                    travelingClass = false;
                }
            } else {
                if (b == 'B' || b == 'C' || b == 'D' || b == 'F' || b == 'I' || b == 'J' || b == 'S' || b == 'Z') {
                    paramCount++;
                } else if (b == 'L') {
                    travelingClass = true;
                    paramCount++;
                } else if (b == ')') {
                    break;
                }
            }
        }
        return paramCount;
    }

    public static String getReturnType(String desc) {
        return null;
    }

}
