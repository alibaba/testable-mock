package com.alibaba.testable.core.model;

import java.io.Serializable;

/**
 * @author flin
 */
public class Pair<L, R> implements Serializable {

    private static final long serialVersionUID = -5197546316467446976L;

    /** Left object */
    private L left;
    /** Right object */
    private R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

    public static <L, R> Pair<L, R> of(L l, R r) {
        return new Pair<L, R>(l, r);
    }
}
