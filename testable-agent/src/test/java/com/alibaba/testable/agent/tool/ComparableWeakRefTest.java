package com.alibaba.testable.agent.tool;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ComparableWeakRefTest {

    @Test
    void should_works_with_weak_hash_map() throws Exception {
        Map<ComparableWeakRef<String>, String> map = new WeakHashMap<ComparableWeakRef<String>, String>();
        map.put(new ComparableWeakRef<String>("a"), "bc");
        map.put(new ComparableWeakRef<String>("ab"), "ac");
        map.put(new ComparableWeakRef<String>("abc"), "bc");
        assertEquals(3, map.size());
        assertEquals("bc", map.get(new ComparableWeakRef<String>("abc")));
        System.gc();
        Thread.sleep(100);
        assertEquals(0, map.size());
        assertNull(map.get(new ComparableWeakRef<String>("abc")));
    }

    @Test
    void should_works_with_hash_set() throws Exception {
        Set<ComparableWeakRef<String>> set = ComparableWeakRef.getWeekHashSet();
        set.add(new ComparableWeakRef<String>("a"));
        set.add(new ComparableWeakRef<String>("ab"));
        set.add(new ComparableWeakRef<String>("abc"));
        assertEquals(3, set.size());
        assertTrue(set.contains(new ComparableWeakRef<String>("ab")));
        System.gc();
        Thread.sleep(100);
        assertEquals(0, set.size());
        assertFalse(set.contains(new ComparableWeakRef<String>("ab")));
    }
}
