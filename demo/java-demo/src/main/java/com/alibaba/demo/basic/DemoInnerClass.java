package com.alibaba.demo.basic;

import java.util.concurrent.*;

/**
 * 演示对内部类的Mock支持
 * Demonstrate support for mocking invocation inside a inner class
 */
public class DemoInnerClass {

    public static class StaticInner {
        /**
         * invocation inside a static inner class
         */
        public String demo() {
            return methodToBeMock();
        }
    }

    public class Inner {
        /**
         * invocation inside a non-static inner class
         */
        public String demo() {
            return methodToBeMock();
        }
    }

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public String callAnonymousInner() throws ExecutionException, InterruptedException {
        /**
         * invocation inside a anonymous inner class
         */
        Future<String> future = executorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return methodToBeMock();
            }
        });
        return future.get();
    }

    public String callLambdaInner() throws ExecutionException, InterruptedException {
        /**
         * invocation inside a lambda inner class
         */
        Future<String> future = executorService.submit(() -> methodToBeMock());
        return future.get();
    }

    public String callInnerDemo() {
        return new Inner().demo();
    }

    public static String methodToBeMock() {
        return "RealCall";
    }

}
