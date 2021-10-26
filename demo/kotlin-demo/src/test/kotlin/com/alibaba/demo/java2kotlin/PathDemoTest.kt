package com.alibaba.demo.java2kotlin

import com.alibaba.testable.core.annotation.MockMethod
import com.alibaba.testable.core.matcher.InvokeVerifier.verify
import org.junit.jupiter.api.Test
import java.io.File

class PathDemoTest {

    class Mock {
        @MockMethod
        fun exists(f: File): Boolean {
            return when (f.absolutePath) {
                "/a/b" -> true
                "/a/b/c" -> true
                else -> f.exists()
            }
        }

        @MockMethod
        fun isDirectory(f: File): Boolean {
            return when (f.absolutePath) {
                "/a/b/c" -> true
                else -> f.isDirectory
            }
        }

        @MockMethod
        fun delete(f: File): Boolean {
            return true
        }

        @MockMethod
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
        verify("listFiles").withTimes(2)
        verify("delete").withTimes(4)
    }

}
