package com.alibaba.testable.core.tool;

import com.alibaba.testable.core.error.VerifyFailedError;

/**
 * @author flin
 */
public class InvokeCounter {

    private final int actualCount;

    public InvokeCounter(int actualCount) {
        this.actualCount = actualCount;
    }

    public void times(int expectedCount) {
        if (expectedCount != actualCount) {
            throw new VerifyFailedError(actualCount, expectedCount);
        }
    }

}
