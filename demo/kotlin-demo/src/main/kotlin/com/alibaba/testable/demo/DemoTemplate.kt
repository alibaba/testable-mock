package com.alibaba.testable.demo

import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet

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
