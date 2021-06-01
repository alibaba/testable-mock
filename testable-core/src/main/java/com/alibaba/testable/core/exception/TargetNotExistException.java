package com.alibaba.testable.core.exception;

/**
 * @author flin
 */
public class TargetNotExistException extends RuntimeException {

    private final String className;
    private final String methodName;

    public TargetNotExistException(String message, String className, String methodName) {
        super(message);
        this.className = className;
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getClassName() {
        return className;
    }
}
