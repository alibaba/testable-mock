package com.alibaba.testable.core.tool;

import com.alibaba.testable.core.accessor.PrivateAccessor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OmniAccessorTest {

    @Test
    void should_generate_member_index() {
        List<String> index = PrivateAccessor.invokeStatic(OmniAccessor.class, "generateMemberIndex", DemoParent.class);
        assertEquals(10, index.size());
        assertEquals("/c1{DemoChild}", index.get(0));
        assertEquals("/c1{DemoChild}/gc1{DemoGrandChild}", index.get(1));
        assertEquals("/c1{DemoChild}/gc2{DemoGrandChild}", index.get(2));
        assertEquals("/c2{DemoChild}", index.get(3));
        assertEquals("/c2{DemoChild}/gc1{DemoGrandChild}", index.get(4));
        assertEquals("/c2{DemoChild}/gc2{DemoGrandChild}", index.get(5));
        assertEquals("/sc{SubChild}", index.get(6));
        assertEquals("/sc{SubChild}/gc{DemoGrandChild}", index.get(7));
        assertEquals("/ssc{StaticSubChild}", index.get(8));
        assertEquals("/ssc{StaticSubChild}/gc{DemoGrandChild}", index.get(9));
    }

    @Test
    void should_to_pattern() {

    }

    @Test
    void should_to_parent() {
        assertEquals("", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toParent", ""));
        assertEquals("", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toParent", "abc"));
        assertEquals("abc", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toParent", "abc/xyz"));
        assertEquals("abc/def", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toParent", "abc/def/xyz"));
        assertEquals("/abc/def", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toParent", "/abc/def/xyz"));
    }

    @Test
    void should_to_child() {
        assertEquals("", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toChild", ""));
        assertEquals("abc", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toChild", "abc"));
        assertEquals("xyz", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toChild", "abc/xyz"));
        assertEquals("xyz", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toChild", "abc/def/xyz"));
        assertEquals("xyz", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toChild", "/abc/def/xyz"));
    }

    @Test
    void should_get_by_path() {

    }

    @Test
    void should_set_by_path() {

    }

}
