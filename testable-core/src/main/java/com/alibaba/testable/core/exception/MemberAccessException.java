package com.alibaba.testable.core.exception;

import java.lang.reflect.InvocationTargetException;

/**
 * @author flin
 */
public class MemberAccessException extends RuntimeException {

    public MemberAccessException(String message) {
        super(message);
    }

    public MemberAccessException(String message, Throwable cause) {
        super(message, getRootCause(cause));
    }

    private static Throwable getRootCause(Throwable cause) {
        if (cause instanceof InvocationTargetException) {
            return ((InvocationTargetException)cause).getTargetException();
        } else {
            return cause;
        }
    }

}
