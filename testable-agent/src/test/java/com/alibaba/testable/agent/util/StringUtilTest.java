package com.alibaba.testable.agent.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilTest {

    @Test
    void should_repeat_text() {
        assertEquals("", StringUtil.repeat("abc", 0));
        assertEquals("abc", StringUtil.repeat("abc", 1));
        assertEquals("abcabcabc", StringUtil.repeat("abc", 3));
    }

}
