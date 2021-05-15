package com.alibaba.testable.core.exception;

/**
 * @author flin
 */
public class TargetNotExistException extends RuntimeException {

    private String methodName;

    public TargetNotExistException(String message, String methodName) {
        super(message);
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }
}
