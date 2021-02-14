package com.alibaba.testable.core.matcher;

import com.alibaba.testable.core.error.VerifyFailedError;
import com.alibaba.testable.core.model.Verification;
import com.alibaba.testable.core.util.MockContextUtil;
import com.alibaba.testable.core.util.TestableUtil;

import java.security.InvalidParameterException;
import java.util.List;

/**
 * @author flin
 */
public class InvokeVerifier {

    private final List<Object[]> records;
    private Verification lastVerification = null;

    private InvokeVerifier(List<Object[]> records) {
        this.records = records;
    }

    /**
     * Get counter to check whether specified mock method invoked
     * @param mockMethodName name of a mock method
     * @return the verifier object
     */
    public static InvokeVerifier verify(String mockMethodName) {
        return new InvokeVerifier(MockContextUtil.context.get().invokeRecord.get(mockMethodName));
    }

    /**
     * Expect mock method invoked with specified parameters
     * @param args parameters to compare
     * @return the verifier object
     */
    public InvokeVerifier with(Object... args) {
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
        lastVerification = new Verification(args, false);
        return this;
    }

    /**
     * Expect next mock method call was invoked with specified parameters
     * @param args parameters to compare
     * @return the verifier object
     */
    public InvokeVerifier withInOrder(Object... args) {
        withInternal(args, 0);
        lastVerification = new Verification(args, true);
        return this;
    }

    /**
     * Expect mock method had never invoked with specified parameters
     * @param args parameters to compare
     * @return the verifier object
     */
    public InvokeVerifier without(Object... args) {
        for (Object[] r : records) {
            if (r.length == args.length) {
                for (int i = 0; i < r.length; i++) {
                    if (!matches(args[i], r[i])) {
                        break;
                    }
                    if (i == r.length - 1) {
                        // all arguments are equal
                        throw new VerifyFailedError("was invoked with " + desc(args));
                    }
                }
            }
        }
        return this;
    }

    /**
     * Expect mock method have been invoked specified times
     * @param expectedCount times to compare
     * @return the verifier object
     */
    public InvokeVerifier withTimes(int expectedCount) {
        if (expectedCount != records.size()) {
            throw new VerifyFailedError("times: " + expectedCount, "times: " + records.size());
        }
        lastVerification = null;
        return this;
    }

    /**
     * Expect several consecutive invocations with the same parameters
     * @param count number of invocations
     * @return the verifier object
     */
    public InvokeVerifier times(int count) {
        if (lastVerification == null) {
            // when used independently, equals to `withTimes()`
            System.out.println("Warning: [" + TestableUtil.previousStackLocation() + "] using \"times()\" method "
                + "without \"with()\" or \"withInOrder()\" is not recommended, please use \"withTimes()\" instead.");
            return withTimes(count);
        }
        if (count < 2) {
            throw new InvalidParameterException("should only use times() method with count equal or larger than 2.");
        }
        for (int i = 0; i < count - 1; i++) {
            if (lastVerification.inOrder) {
                withInOrder(lastVerification.parameters);
            } else {
                with(lastVerification.parameters);
            }
        }
        lastVerification = null;
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
            if (!(args[i] instanceof InvokeMatcher || args[i].getClass().equals(record[i].getClass()))) {
                throw new VerifyFailedError("parameter " + (i + 1) + " type mismatch",
                    ": " + args[i].getClass(), ": " + record[i].getClass());
            }
            if (!matches(args[i], record[i])) {
                throw new VerifyFailedError("parameter " + (i + 1) + " mismatched", desc(args), desc(record));
            }
        }
        records.remove(order);
    }

    private boolean matches(Object expectValue, Object realValue) {
        return expectValue instanceof InvokeMatcher ?
                ((InvokeMatcher) expectValue).matchFunction.check(realValue) :
                expectValue.equals(realValue);
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
