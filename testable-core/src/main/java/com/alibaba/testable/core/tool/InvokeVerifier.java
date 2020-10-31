package com.alibaba.testable.core.tool;

import com.alibaba.testable.core.error.VerifyFailedError;

import java.util.List;

/**
 * @author flin
 */
public class InvokeVerifier {

    private final List<Object[]> records;

    public InvokeVerifier(List<Object[]> records) {
        this.records = records;
    }

    public InvokeVerifier with(Object arg1) {
        with(new Object[]{arg1});
        return this;
    }

    public InvokeVerifier with(Object arg1, Object arg2) {
        with(new Object[]{arg1, arg2});
        return this;
    }

    public InvokeVerifier with(Object arg1, Object arg2, Object arg3) {
        with(new Object[]{arg1, arg2, arg3});
        return this;
    }

    public InvokeVerifier with(Object arg1, Object arg2, Object arg3, Object arg4) {
        with(new Object[]{arg1, arg2, arg3, arg4});
        return this;
    }

    public InvokeVerifier with(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
        with(new Object[]{arg1, arg2, arg3, arg4, arg5});
        return this;
    }

    public InvokeVerifier with(Object[] args) {
        if (records.isEmpty()) {
            throw new VerifyFailedError("has not more invoke");
        }
        Object[] record = records.get(0);
        if (record.length != args.length) {
            throw new VerifyFailedError(desc(args), desc(record));
        }
        for (int i = 0; i < args.length; i++) {
            if (!args[i].getClass().equals(record[i].getClass())) {
                throw new VerifyFailedError("parameter " + (i + 1) + " type mismatch",
                    ": " + args[i].getClass(), ": " + record[i].getClass());
            }
            if (!args[i].equals(record[i])) {
                throw new VerifyFailedError("parameter " + (i + 1) + " mismatched", desc(args), desc(record));
            }
        }
        records.remove(0);
        return this;
    }

    private String desc(Object[] args) {
        StringBuilder sb = new StringBuilder(": ");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(args[i]);
        }
        return sb.toString();
    }

    public InvokeVerifier times(int expectedCount) {
        if (expectedCount != records.size()) {
            throw new VerifyFailedError("times: " + records.size(), "times: " + expectedCount);
        }
        return this;
    }

}
