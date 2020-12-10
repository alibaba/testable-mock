package com.alibaba.testable.demo

import com.alibaba.testable.core.annotation.MockWith
import com.alibaba.testable.core.annotation.TestableMock
import com.alibaba.testable.core.model.MockDiagnose
import com.alibaba.testable.core.tool.TestableConst
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

/**
 * 演示模板方法可以被Mock
 * Demonstrate template method can be mocked
 */
@MockWith(diagnose = MockDiagnose.ENABLE)
internal class DemoTemplateTest {

    private val demoTemplate = DemoTemplate()

    @TestableMock
    private fun <T> getList(self: DemoTemplate, value: T): List<T> {
        return mutableListOf((value.toString() + "_mock_list") as T)
    }

    @TestableMock
    private fun <K, V> getMap(self: DemoTemplate, key: K, value: V): Map<K, V> {
        return mutableMapOf(key to (value.toString() + "_mock_map") as V)
    }

    @TestableMock(targetMethod = TestableConst.CONSTRUCTOR)
    private fun newHashSet(): HashSet<*> {
        val set = HashSet<Any>()
        set.add("insert_mock")
        return set
    }

    @TestableMock
    private fun <E> add(s: MutableSet<E>, e: E): Boolean {
        s.add((e.toString() + "_mocked") as E)
        return true
    }

    @Test
    fun should_able_to_mock_single_template_method() {
        val res = demoTemplate.singleTemplateMethod()
        Assertions.assertEquals("demo_mock_list", res)
    }

    @Test
    fun should_able_to_mock_double_template_method() {
        val res = demoTemplate.doubleTemplateMethod()
        Assertions.assertEquals("testable_mock_map", res)
    }

    @Test
    fun should_able_to_mock_new_template_method() {
        val res = demoTemplate.newTemplateMethod()
        Assertions.assertEquals(2, res.size)
        val iterator = res.stream().iterator()
        Assertions.assertEquals("insert_mock", iterator.next())
        Assertions.assertEquals("world_mocked", iterator.next())
    }

}
