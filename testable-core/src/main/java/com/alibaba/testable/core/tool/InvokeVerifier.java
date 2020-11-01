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
        return with(new Object[]{arg1});
    }

    public InvokeVerifier with(Object arg1, Object arg2) {
        return with(new Object[]{arg1, arg2});
    }

    public InvokeVerifier with(Object arg1, Object arg2, Object arg3) {
        return with(new Object[]{arg1, arg2, arg3});
    }

    public InvokeVerifier with(Object arg1, Object arg2, Object arg3, Object arg4) {
        return with(new Object[]{arg1, arg2, arg3, arg4});
    }

    public InvokeVerifier with(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
        return with(new Object[]{arg1, arg2, arg3, arg4, arg5});
    }

    public InvokeVerifier withInOrder(Object arg1) {
        return withInOrder(new Object[]{arg1});
    }

    public InvokeVerifier withInOrder(Object arg1, Object arg2) {
        return withInOrder(new Object[]{arg1, arg2});
    }

    public InvokeVerifier withInOrder(Object arg1, Object arg2, Object arg3) {
        return withInOrder(new Object[]{arg1, arg2, arg3});
    }

    public InvokeVerifier withInOrder(Object arg1, Object arg2, Object arg3, Object arg4) {
        return withInOrder(new Object[]{arg1, arg2, arg3, arg4});
    }

    public InvokeVerifier withInOrder(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
        return withInOrder(new Object[]{arg1, arg2, arg3, arg4, arg5});
    }

    public InvokeVerifier with(Object[] args) {
        boolean found = false;
        for (int i = 0; i < records.size(); i++) {
            try {
                withInternal(args, i);
                found = true;
                break;
            } catch (AssertionError e) {
                // continue
            }
        }
        if (!found) {
            throw new VerifyFailedError("has not invoke with " + desc(args));
        }
        return this;
    }

    public InvokeVerifier withInOrder(Object[] args) {
        withInternal(args, 0);
        return this;
    }

    public InvokeVerifier withTimes(int expectedCount) {
        if (expectedCount != records.size()) {
            throw new VerifyFailedError("times: " + records.size(), "times: " + expectedCount);
        }
        return this;
    }

    private void withInternal(Object[] args, int order) {
        if (records.isEmpty()) {
            throw new VerifyFailedError("has not more invoke");
        }
        Object[] record = records.get(order);
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
        records.remove(order);
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

}
