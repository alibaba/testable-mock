package com.alibaba.testable.processor.model;

public enum MemberType {

    /**
     * Private member or final member
     */
    PRIVATE_OR_FINAL,

    /**
     * Static private member or Static final member
     */
    STATIC_PRIVATE,

    /**
     * Non-private member
     */
    NON_PRIVATE

}
