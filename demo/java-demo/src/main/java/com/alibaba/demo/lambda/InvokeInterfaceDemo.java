package com.alibaba.demo.lambda;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author jim
 */
public class InvokeInterfaceDemo implements BaseDemo {

    public void collectionInterfaceDefaultOrStatic() {
        blackHole(collectionStreamTest());
        blackHole(arraysStreamTest());
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

    public Object collectionStreamTest() {
        List<List<String>> testList = new ArrayList<>();
        List<String> fooList = new ArrayList<>();
        fooList.add("123");
        fooList.add("456");
        testList.add(fooList);
        return testList.stream()
                //.flatMap(v -> v.stream())
                .flatMap(Collection::stream)
                .map(Double::valueOf)
                .map(BigDecimal::new)
                .reduce(BigDecimal::add);
    }

    public Object arraysStreamTest() {
        List<String[]> zz = new ArrayList<>();
        zz.add(new String[]{"1", "2", "3"});
        return zz.stream()
                .flatMap(Arrays::stream)
                .map(Double::valueOf)
                .map(BigDecimal::new)
                .reduce(BigDecimal::add);
    }

    public void objectStaticMethodReference() {
        List<Boolean> f = new ArrayList<>();
        f.add(false);
        f.add(false);
        f.add(false);
        blackHole(f.stream()
                .reduce(Boolean::logicalAnd)
                .get()
        );
    }

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
