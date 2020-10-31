package com.alibaba.testable.core.error;

/**
 * @author flin
 */
public class VerifyFailedError extends AssertionError {

    public VerifyFailedError(String message) {
        super(getErrorMessage(message));
    }

    public VerifyFailedError(String expected, String actual) {
        super(getErrorMessage(expected, actual));
    }

    public VerifyFailedError(String message, String expected, String actual) {
        super(getErrorMessage(message) + getErrorMessage(expected, actual));
    }

    private static String getErrorMessage(String message) {
        return "\n" + message.substring(0, 1).toUpperCase() + message.substring(1);
    }

    private static String getErrorMessage(String expected, String actual) {
        return "\nExpected " + expected + "\n  Actual " + actual;
    }

}
