package com.alibaba.demo.lambda;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jim
 */
public class StaticInstanceReference {

    private static final StaticClassA STATIC_CLASS_A = new StaticClassA();

    public void staticMethodReference() {
        //StaticClassA a = new StaticClassA();
        //consumesRun(a::doIt);
        consumesRun(STATIC_CLASS_A::doIt);
        consumesFunction1(STATIC_CLASS_A::function1);
        consumesFunction2(STATIC_CLASS_A::function2);
        consumesFunction3(STATIC_CLASS_A::function3);

    }

    public void collectionInterfaceDefaultOrStatic() {
        blackHole(invokeInterfaceTest());
        blackHole(interfaceStaticMethodTest());
    }

    public void interfaceDefault() {
        ILambda l = new LambdaFoo();
        consumesRun(l::run);
        consumesFunction1(l::function1);
    }

    public void interfaceStatic() {
        consumesRun(ILambda::staticRun);
        consumesFunction1(ILambda::staticFunction1);
    }

    private void consumesRun(Runnable r) {
        r.run();
    }

    private <T> void consumesFunction1(Consumer<T> r) {
        r.accept(null);
    }

    private <T, R> void consumesFunction2(Function<T, R> r) {
        r.apply(null);
    }

    private <T1, T2, R> void consumesFunction3(BiFunction<T1, T2, R> r) {
        r.apply(null, null);
    }

    public static class StaticClassA {
        public void doIt() {
        }

        public void function1(String s) {

        }

        public Integer function2(String s) {
            return 1;
        }

        public Integer function3(String s, Double d) {
            return 1;
        }
    }

    public static class XBean {
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

    public void foo() {
        List<XBean> testList = Collections.emptyList();
        //noinspection RedundantOperationOnEmptyContainer
        List<Long> response = testList.stream().map(XBean::getId).distinct().collect(Collectors.toList());
    }

    public Object invokeInterfaceTest() {
        List<List<String>> zz = new ArrayList<>();
        zz.add(new ArrayList<>());
        return zz.stream()
                //.flatMap(Collection::stream)
                //.flatMap(v -> v.stream())
                .flatMap(Collection::stream)
                .map(Double::valueOf)
                .map(BigDecimal::new)
                .reduce(BigDecimal::add);
    }

    public Object interfaceStaticMethodTest() {
        List<String[]> zz = new ArrayList<>();
        zz.add(new String[]{"1"});
        return zz.stream()
                .flatMap(Arrays::stream)
                .map(Double::valueOf)
                .map(BigDecimal::new)
                .reduce(BigDecimal::add);
    }

    public Object objectStaticMethodReference() {
        List<List<Boolean>> zz = new ArrayList<>();
        zz.add(new ArrayList<>());
        return zz.stream()
                .flatMap(Collection::stream)
                .reduce(Boolean::logicalAnd);
    }

    private void blackHole(Object... ignore) {}

    public interface ILambda {
        default void run() {

        }

        default void function1(String s) {

        }

        static void staticRun() {

        }

        static void staticFunction1(String s) {

        }
    }

    public static class LambdaFoo implements ILambda {

    }
}
