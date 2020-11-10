package com.alibaba.testable.demo.service

import com.alibaba.testable.core.accessor.PrivateAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


internal class DemoPrivateAccessServiceTest {

    private val demoService = DemoPrivateAccessService()

    @Test
    fun should_able_to_access_private_method() {
        assertEquals("hello - 1", PrivateAccessor.invoke(demoService, "privateFunc", "hello", 1))
    }

    @Test
    fun should_able_to_access_private_field() {
        PrivateAccessor.set(demoService, "count", 3)
        assertEquals("5", demoService.privateFieldAccessFunc())
        assertEquals(5, PrivateAccessor.get(demoService, "count"))
    }

    @Test
    fun should_able_to_update_final_field() {
        PrivateAccessor.set(demoService, "pi", 4.13)
        assertEquals(4.13, demoService.pi)
    }

}
