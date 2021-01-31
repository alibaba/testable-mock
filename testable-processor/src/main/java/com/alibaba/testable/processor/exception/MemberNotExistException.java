package com.alibaba.testable.processor.exception;

/**
 * @author flin
 */
public class MemberNotExistException extends RuntimeException {

    public MemberNotExistException(String type, String className, String target) {
        super(String.format("%s \"%s\" not exist in class \"%s\"", type, target, className));
    }

    public MemberNotExistException(String type, String className, String target, int count) {
        super(String.format("%s \"%s\" with %d %s not exist in class \"%s\"",
            type, target, count, parameters(count), className));
    }

    private static String parameters(int count) {
        return count > 1 ? "parameters" : "parameter";
    }

}
