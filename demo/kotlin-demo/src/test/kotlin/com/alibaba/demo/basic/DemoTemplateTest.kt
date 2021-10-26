package com.alibaba.demo.basic

import com.alibaba.testable.core.annotation.MockConstructor
import com.alibaba.testable.core.annotation.MockMethod
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * 演示模板方法的Mock场景
 * Demonstrate scenario of mocking template method
 */
internal class DemoTemplateTest {

    private val demoTemplate = DemoTemplate()

    class Mock {
        @MockMethod
        private fun <T> getList(self: DemoTemplate, value: T): List<T> {
            return mutableListOf((value.toString() + "_mock_list") as T)
        }

        @MockMethod
        private fun <K, V> getMap(self: DemoTemplate, key: K, value: V): Map<K, V> {
            return mutableMapOf(key to (value.toString() + "_mock_map") as V)
        }

        @MockConstructor
        private fun newHashSet(): HashSet<*> {
            val set = HashSet<Any>()
            set.add("insert_mock")
            return set
        }

        @MockMethod
        private fun <E> add(s: MutableSet<E>, e: E): Boolean {
            s.add((e.toString() + "_mocked") as E)
            return true
        }
    }

    @Test
    fun should_mock_single_template_method() {
        val res = demoTemplate.singleTemplateMethod()
        assertEquals("demo_mock_list", res)
    }

    @Test
    fun should_mock_double_template_method() {
        val res = demoTemplate.doubleTemplateMethod()
        assertEquals("testable_mock_map", res)
    }

    @Test
    fun should_mock_new_template_method() {
        val res = demoTemplate.newTemplateMethod()
        assertEquals(2, res.size)
        val iterator = res.stream().iterator()
        assertEquals("insert_mock", iterator.next())
        assertEquals("world_mocked", iterator.next())
    }

}
