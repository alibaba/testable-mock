package com.alibaba.testable.demo.util

import org.junit.jupiter.api.Test
import com.alibaba.testable.core.annotation.TestableMock
import com.alibaba.testable.core.tool.TestableTool.verify
import java.io.File

class PathUtilTest {

    @TestableMock
    fun exists(f: File): Boolean {
        return when (f.absolutePath) {
            "/a/b" -> true
            "/a/b/c" -> true
            else -> f.exists()
        }
    }

    @TestableMock
    fun isDirectory(f: File): Boolean {
        return when (f.absolutePath) {
            "/a/b/c" -> true
            else -> f.isDirectory
        }
    }

    @TestableMock
    fun delete(f: File): Boolean {
        return true
    }

    @TestableMock
    fun listFiles(f: File): Array<File>? {
        return when (f.absolutePath) {
            "/a/b" -> arrayOf(File("/a/b/c"), File("/a/b/d"))
            "/a/b/c" -> arrayOf(File("/a/b/c/e"))
            else -> f.listFiles()
        }
    }

    @Test
    fun should_able_to_mock_java_method_invoke_in_kotlin() {
        PathUtil.deleteRecursively(File("/a/b/"))
        verify("listFiles").times(2)
        verify("delete").times(4)
    }

}
