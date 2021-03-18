package com.alibaba.testable.processor.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilTest {

    @Test
    void should_join_string() {
        List<String> list = new ArrayList<String>(4);
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");
        assertEquals("a-b-c-d", StringUtil.join(list, "-"));
    }
}
