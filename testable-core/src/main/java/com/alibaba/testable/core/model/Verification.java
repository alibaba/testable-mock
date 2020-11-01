package com.alibaba.testable.core.model;

/**
 * @author flin
 */
public class Verification {

    public Object[] parameters;

    public boolean inOrder;

    public Verification(Object[] parameters, boolean inOrder) {
        this.parameters = parameters;
        this.inOrder = inOrder;
    }
}
