package com.alibaba.demo.basic;

import com.alibaba.testable.core.annotation.MockConstructor;
import com.alibaba.testable.core.annotation.MockMethod;
import com.alibaba.demo.basic.DemoTemplate;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 演示模板方法的Mock场景
 * Demonstrate scenario of mocking template method
 */
class DemoTemplateTest {

    private DemoTemplate demoTemplate = new DemoTemplate();

    public static class Mock {
        /* 第一种写法：使用泛型定义 */
        /* First solution: use generics type */

        @MockMethod
        private <T> List<T> getList(DemoTemplate self, T value) {
            return new ArrayList<T>() {{ add((T)(value.toString() + "_mock_list")); }};
        }

        @MockMethod
        private <K, V> Map<K, V> getMap(DemoTemplate self, K key, V value) {
            return new HashMap<K, V>() {{ put(key, (V)(value.toString() + "_mock_map")); }};
        }

        @MockConstructor
        private <T> HashSet<T> newHashSet() {
            HashSet<T> set = new HashSet<>();
            set.add((T)"insert_mock");
            return set;
        }

        @MockMethod
        private <E> boolean add(Set s, E e) {
            s.add(e.toString() + "_mocked");
            return true;
        }

        /* 第二种写法：使用Object类型 */
        /* Second solution: use object type */

        //@MockMethod
        //private List<Object> getList(DemoTemplate self, Object value) {
        //    return new ArrayList<Object>() {{ add(value.toString() + "_mock_list"); }};
        //}
        //
        //@MockMethod
        //private Map<Object, Object> getMap(DemoTemplate self, Object key, Object value) {
        //    return new HashMap<Object, Object>() {{ put(key, value.toString() + "_mock_map"); }};
        //}
        //
        //@MockConstructor
        //private HashSet newHashSet() {
        //    HashSet<Object> set = new HashSet<>();
        //    set.add("insert_mock");
        //    return set;
        //}
        //
        //@MockMethod
        //private boolean add(Set s, Object e) {
        //    s.add(e.toString() + "_mocked");
        //    return true;
        //}
    }

    @Test
    void should_mock_single_template_method() {
        String res = demoTemplate.singleTemplateMethod();
        assertEquals("demo_mock_list", res);
    }

    @Test
    void should_mock_double_template_method() {
        String res = demoTemplate.doubleTemplateMethod();
        assertEquals("testable_mock_map", res);
    }

    @Test
    void should_mock_new_template_method() {
        Set<?> res = demoTemplate.newTemplateMethod();
        assertEquals(2, res.size());
        Iterator<?> iterator = res.stream().iterator();
        assertEquals("insert_mock", iterator.next());
        assertEquals("world_mocked", iterator.next());
    }

}
