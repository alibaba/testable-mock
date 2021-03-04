package com.alibaba.demo.basic

import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

/**
 * 演示对内部类的Mock支持
 * Demonstrate support for mocking invocation inside a inner class
 */
class DemoInnerClass {

    class StaticInner {
        /**
         * invocation inside a static inner class
         */
        fun demo(): String {
            return methodToBeMock()
        }
    }

    inner class Inner {
        /**
         * invocation inside a non-static inner class
         */
        fun demo(): String {
            return methodToBeMock()
        }
    }

    private val executorService = Executors.newSingleThreadExecutor()
    @Throws(ExecutionException::class, InterruptedException::class)
    fun callAnonymousInner(): String {
        /**
         * invocation inside a anonymous inner class
         */
        val future = executorService.submit<String> { methodToBeMock() }
        return future.get()
    }

    fun callInnerDemo(): String {
        return Inner().demo()
    }

    companion object {
        fun methodToBeMock(): String {
            return "RealCall"
        }
    }
}
