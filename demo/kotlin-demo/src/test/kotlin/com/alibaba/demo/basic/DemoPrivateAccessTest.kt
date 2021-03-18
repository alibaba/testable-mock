package com.alibaba.demo.basic

import com.alibaba.testable.core.accessor.PrivateAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * 演示私有成员访问功能
 * Demonstrate private member access functionality
 */
internal class DemoPrivateAccessTest {

    private val demoPrivateAccess = DemoPrivateAccess()

    @Test
    fun should_access_private_method() {
        val list = listOf("a", "b", "c");
        assertEquals("abc + hello + 1", PrivateAccessor.invoke(demoPrivateAccess, "privateFunc", list, "hello", 1))
    }

    @Test
    fun should_access_private_field() {
        PrivateAccessor.set(demoPrivateAccess, "count", 3)
        assertEquals(3, PrivateAccessor.get(demoPrivateAccess, "count"))
    }

    @Test
    fun should_access_private_static_method() {
        val list = listOf("a", "b", "c");
        assertEquals("hello + 1", PrivateAccessor.invokeStatic(DemoPrivateAccess::class.java, "privateStaticFunc", "hello", 1))
        assertEquals("abc * hello * 1", PrivateAccessor.invokeStatic(DemoPrivateAccess::class.java, "privateJvmStaticFunc", list, "hello", 1))
    }

    @Test
    fun should_access_private_static_field() {
        PrivateAccessor.setStatic(DemoPrivateAccess::class.java, "staticCount", 3)
        assertEquals(3, PrivateAccessor.getStatic(DemoPrivateAccess::class.java, "staticCount"))
    }

    @Test
    fun should_update_final_field() {
        PrivateAccessor.set(demoPrivateAccess, "pi", 4.13)
        assertEquals(4.13, demoPrivateAccess.pi)
    }

}
