package com.alibaba.testable.agent.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PathUtilTest {

    @Test
    void should_get_sub_folder() {
        assertEquals("/ab/cd", PathUtil.getFirstLevelFolder("/ab", "/ab/cd/ef/gh"));
        assertEquals("c:\\ab\\cd", PathUtil.getFirstLevelFolder("c:\\ab", "c:\\ab\\cd\\ef\\gh"));
        assertEquals("", PathUtil.getFirstLevelFolder("/ab", "/ab"));
        assertEquals("", PathUtil.getFirstLevelFolder("/ab/cd", "/ab"));
    }

}
