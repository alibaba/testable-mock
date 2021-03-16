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
        assertEquals("/c{DemoChild}", index.get(0));
        assertEquals("/c{DemoChild}/gc{DemoGrandChild}", index.get(1));
        assertEquals("/c{DemoChild}/gcs{DemoGrandChild[]}", index.get(2));
        assertEquals("/cs{DemoChild[]}", index.get(3));
        assertEquals("/cs{DemoChild[]}/gc{DemoGrandChild}", index.get(4));
        assertEquals("/cs{DemoChild[]}/gcs{DemoGrandChild[]}", index.get(5));
        assertEquals("/sc{SubChild}", index.get(6));
        assertEquals("/sc{SubChild}/gc{DemoGrandChild}", index.get(7));
        assertEquals("/ssc{StaticSubChild}", index.get(8));
        assertEquals("/ssc{StaticSubChild}/gc{DemoGrandChild}", index.get(9));
    }

    @Test
    void should_match_pattern() {
        assertTrue("abc{Abc}".matches("abc\\{[^}]+\\}"));
        assertTrue("abc{Abc}".matches("[^{]+\\{Abc\\}"));
        assertTrue("abc{Abc[]}/xyz{Xyz[]}".matches("[^{]+\\{Abc\\[\\]\\}/[^{]+\\{Xyz\\[\\]\\}"));
        assertTrue("abc{Abc}/xyz{Xyz}/demo{Demo}".matches("abc\\{[^}]+\\}/[^{]+\\{Xyz\\}/demo\\{[^}]+\\}"));
    }

    @Test
    void should_to_pattern() {
        assertEquals("", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toPattern", ""));
        assertEquals("abc\\{[^}]+\\}", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toPattern", "abc"));
        assertEquals("[^{]+\\{Abc\\}", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toPattern", "{Abc}"));
        assertEquals("abc\\{[^}]+\\}/xyz\\{[^}]+\\}", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toPattern", "abc/xyz"));
        assertEquals("[^{]+\\{Abc\\}/xyz\\{[^}]+\\}", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toPattern", "{Abc}/xyz"));
        assertEquals("abc\\{[^}]+\\}/[^{]+\\{Xyz\\}", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toPattern", "abc/{Xyz}"));
        assertEquals("[^{]+\\{Abc\\}/[^{]+\\{Xyz\\}", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toPattern", "{Abc}/{Xyz}"));
        assertEquals("[^{]+\\{Abc\\[\\]\\}/[^{]+\\{Xyz\\[\\]\\}", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toPattern", "{Abc[]}/{Xyz[]}"));
        assertEquals("abc\\{[^}]+\\}/[^{]+\\{Xyz\\}/demo\\{[^}]+\\}", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toPattern", "abc/{Xyz}/demo"));
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
