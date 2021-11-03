package com.alibaba.demo.java2kotlin

import com.alibaba.testable.core.annotation.MockInvoke
import com.alibaba.testable.core.matcher.InvocationVerifier.verifyInvoked
import org.junit.jupiter.api.Test
import java.io.File

class PathDemoTest {

    class Mock {
        @MockInvoke
        fun exists(f: File): Boolean {
            return when (f.absolutePath) {
                "/a/b" -> true
                "/a/b/c" -> true
                else -> f.exists()
            }
        }

        @MockInvoke
        fun isDirectory(f: File): Boolean {
            return when (f.absolutePath) {
                "/a/b/c" -> true
                else -> f.isDirectory
            }
        }

        @MockInvoke
        fun delete(f: File): Boolean {
            return true
        }

        @MockInvoke
        fun listFiles(f: File): Array<File>? {
            return when (f.absolutePath) {
                "/a/b" -> arrayOf(File("/a/b/c"), File("/a/b/d"))
                "/a/b/c" -> arrayOf(File("/a/b/c/e"))
                else -> f.listFiles()
            }
        }
    }

    @Test
    fun should_mock_java_method_invoke_in_kotlin() {
        PathDemo.deleteRecursively(File("/a/b/"))
        verifyInvoked("listFiles").withTimes(2)
        verifyInvoked("delete").withTimes(4)
    }

}
