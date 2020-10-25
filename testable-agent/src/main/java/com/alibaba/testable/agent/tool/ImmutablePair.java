package com.alibaba.testable.agent.tool;


/**
 * @author flin
 */
public class ImmutablePair<L, R> {

    /** Left object */
    public final L left;
    /** Right object */
    public final R right;

    public ImmutablePair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> ImmutablePair<L, R> of(L l, R r) {
        return new ImmutablePair<L, R>(l, r);
    }
}
