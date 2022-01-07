package com.alibaba.demo.lambda;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author jim
 */
public interface BaseDemo {
    default void blackHole(Object... ignore) {}

    default void consumesRun(Runnable r) {
        r.run();
    }

    default void consumesFunction0(Runnable f) {
        f.run();
    }

    default <T> void consumesFunction1(Consumer<T> f) {
        f.accept(null);
    }

    default <T, R> void consumesFunction2(Function<T, R> f) {
        f.apply(null);
    }

    default <T1, T2, R> void consumesFunction3(BiFunction<T1, T2, R> f) {
        f.apply(null, null);
    }

    default <T> void consumesSupplier(Supplier<T> supplier) {
        supplier.get();
    }

    default <T, R> void consumesTwoFunction2(Function<T, R> f1, Function<T, R> f2) {
        f1.apply(null);
        f2.apply(null);
    }
}
