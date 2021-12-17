package com.alibaba.demo.lambda;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jim
 */
public class StaticInstanceReference {

    private static A a = new A();

    public void staticMethodReference() {
        //consumesRun(() -> a.doIt());
        //A b = new A();
        //consumesRun(b::doIt);
        consumesRun(a::doIt);
        consumesFunction1(a::function1);
        consumesFunction2(a::function2);
        consumesFunction3(a::function3);
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

   public static class A {
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

}
