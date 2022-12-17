package com.alibaba.testable.core.tool;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.alibaba.testable.core.tool.CollectionTool.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectionToolTest {

    @Test
    void should_slice_array() {
        String[] fullWords = arrayOf("this", "is", "a", "simple", "example");
        String[] partOfWords = slice(fullWords, 1, 3);
        assertEquals(3, partOfWords.length);
        assertEquals("is", partOfWords[0]);
        assertEquals("a", partOfWords[1]);
        assertEquals("simple", partOfWords[2]);

        Integer[] fibonacci = arrayOf(1, 1, 2, 3, 5, 8, 13, 21);
        Integer[] partOfFibonacci = slice(fibonacci, 4);
        assertEquals(4, partOfFibonacci.length);
        assertEquals(5, partOfFibonacci[0]);
        assertEquals(8, partOfFibonacci[1]);
        assertEquals(13, partOfFibonacci[2]);
        assertEquals(21, partOfFibonacci[3]);
    }

    @Test
    void should_create_array() {
        String[] manyWords = arrayOf("this", "is", "an", "example");
        assertEquals(4, manyWords.length);
        assertEquals("this", manyWords[0]);
        assertEquals("is", manyWords[1]);
        assertEquals("an", manyWords[2]);
        assertEquals("example", manyWords[3]);

        Double[] manyValues = arrayOf(1.0D, 2.0D, 3.0D);
        assertEquals(3, manyValues.length);
        assertEquals(1.0D, manyValues[0]);
        assertEquals(2.0D, manyValues[1]);
        assertEquals(3.0D, manyValues[2]);
    }

    @Test
    void should_create_list() {
        List<String> manyWords = listOf("this", "is", "an", "example");
        assertEquals(4, manyWords.size());
        assertEquals("this", manyWords.get(0));
        assertEquals("is", manyWords.get(1));
        assertEquals("an", manyWords.get(2));
        assertEquals("example", manyWords.get(3));

        List<Double> manyValues = listOf(1.0D, 2.0D, 3.0D);
        assertEquals(3, manyValues.size());
        assertEquals(1.0D, manyValues.get(0));
        assertEquals(2.0D, manyValues.get(1));
        assertEquals(3.0D, manyValues.get(2));
    }

    @Test
    void should_create_set() {
        Set<String> manyWords = setOf("this", "is", "an", "example");
        assertEquals(4, manyWords.size());
        assertTrue(manyWords.contains("this"));
        assertTrue(manyWords.contains("is"));
        assertTrue(manyWords.contains("an"));
        assertTrue(manyWords.contains("example"));

        Set<Double> manyValues = setOf(1.0D, 2.0D, 3.0D);
        assertEquals(3, manyValues.size());
        assertTrue(manyValues.contains(1.0D));
        assertTrue(manyValues.contains(2.0D));
        assertTrue(manyValues.contains(3.0D));
    }

    @Test
    void should_create_map() {
        Map<String, String> manyWords = mapOf(
                entryOf("language", "java"),
                entryOf("type", "unittest"),
                entryOf("library", "testable")
        );
        assertEquals(3, manyWords.size());
        assertEquals("java", manyWords.get("language"));
        assertEquals("unittest", manyWords.get("type"));
        assertEquals("testable", manyWords.get("library"));

        Map<Integer, Double> manyValues = mapOf(
                entryOf(1, 10.12D),
                entryOf(2, 20.24D),
                entryOf(3, 30.36D)
        );
        assertEquals(3, manyValues.size());
        assertEquals(10.12D, manyValues.get(1));
        assertEquals(20.24D, manyValues.get(2));
        assertEquals(30.36D, manyValues.get(3));
    }

    @Test
    void should_create_ordered_map() {
        Map<String, String> manyWords = orderedMapOf(
                entryOf("language", "java"),
                entryOf("type", "unittest"),
                entryOf("library", "testable")
        );
        assertEquals(3, manyWords.size());
        String[] wordKeys = manyWords.keySet().toArray(new String[0]);
        assertEquals("language", wordKeys[0]);
        assertEquals("type", wordKeys[1]);
        assertEquals("library", wordKeys[2]);

        Map<Integer, Double> manyValues = orderedMapOf(
                entryOf(1, 10.12D),
                entryOf(2, 20.24D),
                entryOf(3, 30.36D)
        );
        assertEquals(3, manyValues.size());
        Integer[] valueKeys = manyValues.keySet().toArray(new Integer[0]);
        assertEquals(1, valueKeys[0]);
        assertEquals(2, valueKeys[1]);
        assertEquals(3, valueKeys[2]);
    }

}