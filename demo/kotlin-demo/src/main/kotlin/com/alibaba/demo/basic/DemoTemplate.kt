package com.alibaba.demo.basic

import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet

/**
 * 演示模板方法的Mock场景
 * Demonstrate scenario of mocking template method
 */
class DemoTemplate {
    private fun <T> getList(value: T): List<T> {
        val l: MutableList<T> = ArrayList()
        l.add(value)
        return l
    }

    private fun <K, V> getMap(key: K, value: V): Map<K, V> {
        val m: MutableMap<K, V> = HashMap()
        m[key] = value
        return m
    }

    fun singleTemplateMethod(): String {
        val list = getList("demo")
        return list[0]
    }

    fun doubleTemplateMethod(): String? {
        val map = getMap("hello", "testable")
        return map["hello"]
    }

    fun newTemplateMethod(): Set<*> {
        val set: MutableSet<String> = HashSet()
        set.add("world")
        return set
    }
}
