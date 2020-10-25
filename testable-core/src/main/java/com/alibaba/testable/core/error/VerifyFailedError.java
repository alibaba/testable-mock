package com.alibaba.testable.core.error;

/**
 * @author flin
 */
public class VerifyFailedError extends AssertionError {

    public VerifyFailedError(int actualCount, int expectedCount) {
        super(getErrorMessage(actualCount, expectedCount));
    }

    private static String getErrorMessage(int actualCount, int expectedCount) {
        return "\nExpected times : " + expectedCount + "\nActual times   : " + actualCount;
    }

}
