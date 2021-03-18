package com.alibaba.testable.core.exception;

import java.lang.reflect.InvocationTargetException;

/**
 * @author flin
 */
public class ClassConstructionException extends RuntimeException {

    public ClassConstructionException(String message) {
        super(message);
    }

    public ClassConstructionException(String message, Throwable cause) {
        super(message, cause);
    }

    private static Throwable getRootCause(Throwable cause) {
        if (cause instanceof InvocationTargetException) {
            return ((InvocationTargetException)cause).getTargetException();
        } else {
            return cause;
        }
    }

}
