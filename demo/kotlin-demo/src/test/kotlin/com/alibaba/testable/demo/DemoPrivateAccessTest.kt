package com.alibaba.testable.demo

import com.alibaba.testable.core.accessor.PrivateAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


internal class DemoPrivateAccessTest {

    private val demoPrivateAccess = DemoPrivateAccess()

    @Test
    fun should_able_to_access_private_method() {
        assertEquals("hello - 1", PrivateAccessor.invoke(demoPrivateAccess, "privateFunc", "hello", 1))
    }

    @Test
    fun should_able_to_access_private_field() {
        PrivateAccessor.set(demoPrivateAccess, "count", 3)
        assertEquals("5", demoPrivateAccess.privateFieldAccessFunc())
        assertEquals(5, PrivateAccessor.get(demoPrivateAccess, "count"))
    }

    @Test
    fun should_able_to_update_final_field() {
        PrivateAccessor.set(demoPrivateAccess, "pi", 4.13)
        assertEquals(4.13, demoPrivateAccess.pi)
    }

}
