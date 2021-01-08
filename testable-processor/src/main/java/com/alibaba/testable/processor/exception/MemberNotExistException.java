package com.alibaba.testable.processor.exception;

/**
 * @author flin
 */
public class MemberNotExistException extends RuntimeException {

    public MemberNotExistException(String type, String className, String target) {
        super(type + " \"" + target + "\" not exist in class \"" + className + "\"");
    }

}
