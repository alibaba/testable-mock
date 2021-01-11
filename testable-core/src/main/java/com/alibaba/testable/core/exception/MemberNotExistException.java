package com.alibaba.testable.core.exception;

/**
 * @author flin
 */
public class MemberNotExistException extends RuntimeException {

    public MemberNotExistException(String message) {
        super(message);
    }

    public MemberNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

}
