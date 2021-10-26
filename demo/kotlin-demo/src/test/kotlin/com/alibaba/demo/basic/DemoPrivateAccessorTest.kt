package com.alibaba.demo.basic

import com.alibaba.testable.core.tool.PrivateAccessor.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * 演示私有成员访问功能
 * Demonstrate private member access functionality
 */
internal class DemoPrivateAccessorTest {

    private val demoPrivateAccess = DemoPrivateAccess()

    @Test
    fun should_access_private_method() {
        val list = listOf("a", "b", "c");
        assertEquals("abc + hello + 1", invoke(demoPrivateAccess, "privateFunc", list, "hello", 1))
    }

    @Test
    fun should_access_private_field() {
        set(demoPrivateAccess, "count", 3)
        assertEquals(3, get(demoPrivateAccess, "count"))
    }

    @Test
    fun should_access_private_static_method() {
        val list = listOf("a", "b", "c");
        assertEquals("hello + 1", invokeStatic(DemoPrivateAccess::class.java, "privateStaticFunc", "hello", 1))
        assertEquals("abc * hello * 1", invokeStatic(DemoPrivateAccess::class.java, "privateJvmStaticFunc", list, "hello", 1))
    }

    @Test
    fun should_access_private_static_field() {
        setStatic(DemoPrivateAccess::class.java, "staticCount", 3)
        assertEquals(3, getStatic(DemoPrivateAccess::class.java, "staticCount"))
    }

    @Test
    fun should_update_final_field() {
        set(demoPrivateAccess, "pi", 4.13)
        assertEquals(4.13, demoPrivateAccess.pi)
    }

}
