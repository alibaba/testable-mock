package com.alibaba.testable.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilTest {

    @Test
    void should_repeat_text() {
        assertEquals("", StringUtil.repeat("abc", 0));
        assertEquals("abc", StringUtil.repeat("abc", 1));
        assertEquals("abcabcabc", StringUtil.repeat("abc", 3));
    }

    @Test
    void should_join_text() {
        assertEquals("abc", StringUtil.join("", "a", "b", "c"));
        assertEquals("a", StringUtil.join(",", "a"));
        assertEquals("ab,cd,ef", StringUtil.join(",", new String[]{"ab", "cd", "ef"}));
    }

}
