package com.alibaba.demo.lambda;

/**
 * @author jim
 */
@FunctionalInterface
public interface Function1Throwable<T, R> {
    R apply(T t) throws Throwable;
}
