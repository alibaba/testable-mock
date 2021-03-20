package com.alibaba.testable.core.tool;

import com.alibaba.testable.core.accessor.PrivateAccessor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OmniAccessorTest {

    @Test
    void should_generate_member_index() {
        List<String> index = PrivateAccessor.invokeStatic(OmniAccessor.class, "generateMemberIndex", DemoParent.class);
        assertEquals(16, index.size());
        assertEquals("/c{DemoChild}", index.get(0));
        assertEquals("/c{DemoChild}/gc{DemoGrandChild}", index.get(1));
        assertEquals("/c{DemoChild}/gc{DemoGrandChild}/i{int}", index.get(2));
        assertEquals("/c{DemoChild}/gcs{DemoGrandChild[]}", index.get(3));
        assertEquals("/c{DemoChild}/gcs{DemoGrandChild[]}/i{int}", index.get(4));
        assertEquals("/cs{DemoChild[]}", index.get(5));
        assertEquals("/cs{DemoChild[]}/gc{DemoGrandChild}", index.get(6));
        assertEquals("/cs{DemoChild[]}/gc{DemoGrandChild}/i{int}", index.get(7));
        assertEquals("/cs{DemoChild[]}/gcs{DemoGrandChild[]}", index.get(8));
        assertEquals("/cs{DemoChild[]}/gcs{DemoGrandChild[]}/i{int}", index.get(9));
        assertEquals("/sc{SubChild}", index.get(10));
        assertEquals("/sc{SubChild}/gc{DemoGrandChild}", index.get(11));
        assertEquals("/sc{SubChild}/gc{DemoGrandChild}/i{int}", index.get(12));
        assertEquals("/ssc{StaticSubChild}", index.get(13));
        assertEquals("/ssc{StaticSubChild}/gc{DemoGrandChild}", index.get(14));
        assertEquals("/ssc{StaticSubChild}/gc{DemoGrandChild}/i{int}", index.get(15));
    }

    @Test
    void should_to_pattern() {
        assertEquals(".*/", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toPattern", ""));
        assertEquals(".*/abc\\{[^}]+\\}", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toPattern", "abc"));
        assertEquals(".*/[^{]+\\{Abc\\}", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toPattern", "{Abc}"));
        assertEquals(".*/abc\\{[^}]+\\}/xyz\\{[^}]+\\}", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toPattern", "abc/xyz"));
        assertEquals(".*/[^{]+\\{Abc\\}/xyz\\{[^}]+\\}", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toPattern", "{Abc}/xyz"));
        assertEquals(".*/abc\\{[^}]+\\}/[^{]+\\{Xyz\\}", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toPattern", "abc/{Xyz}"));
        assertEquals(".*/[^{]+\\{Abc\\}/[^{]+\\{Xyz\\}", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toPattern", "{Abc}/{Xyz}"));
        assertEquals(".*/[^{]+\\{Abc\\[\\]\\}/[^{]+\\{Xyz\\[\\]\\}", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toPattern", "{Abc[]}/{Xyz[]}"));
        assertEquals(".*/abc\\{[^}]+\\}/[^{]+\\{Xyz\\[\\]\\}", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toPattern", "abc[1]/{Xyz[]}[2]"));
        assertEquals(".*/abc\\{[^}]+\\}/[^{]+\\{Xyz\\}/demo\\{[^}]+\\}", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toPattern", "abc/{Xyz}/demo"));
        assertEquals(".*/abc\\{[^}]+\\}/de[^{]+\\{[^}]+\\}/[^{]+\\{Xyz\\}", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toPattern", "abc/de*/{Xyz}"));
        assertEquals(".*/abc\\{[^}]+\\}/[^{]+mo\\{[^}]+\\}/[^{]+\\{Xyz\\}", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toPattern", "abc/*mo/{Xyz}"));
        assertEquals(".*/abc\\{[^}]+\\}/d[^{]+o\\{[^}]+\\}/[^{]+\\{Xyz\\}", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toPattern", "abc/d*o/{Xyz}"));
        assertEquals(".*/abc\\{[^}]+\\}/[^{]+\\{De[^}]+\\}/[^{]+\\{Xyz\\}", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toPattern", "abc/{De*}/{Xyz}"));
        assertEquals(".*/abc\\{[^}]+\\}/[^{]+\\{[^}]+mo\\}/[^{]+\\{Xyz\\}", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toPattern", "abc/{*mo}/{Xyz}"));
        assertEquals(".*/abc\\{[^}]+\\}/[^{]+\\{D[^}]+o\\}/[^{]+\\{Xyz\\}", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toPattern", "abc/{D*o}/{Xyz}"));
        assertEquals(".*/abc\\{[^}]+\\}/[^/]+/[^{]+\\{Xyz\\}/[^/]+/demo\\{[^}]+\\}", PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "toPattern", "abc/*/{Xyz}/*/demo"));
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
    void should_get_full_query_segments() {
        String[] querySegments = new String[] { "c", "d" };
        String[] memberSegments = new String[] { "a{A}", "b{B}", "c{C}", "d{D}" };
        String[] fullQuerySegments = PrivateAccessor.invokeStatic(OmniAccessor.class, "calculateFullQueryPath", querySegments, memberSegments);
        assertEquals(5, fullQuerySegments.length);
        assertEquals("", fullQuerySegments[0]);
        assertEquals("", fullQuerySegments[1]);
        assertEquals("", fullQuerySegments[2]);
        assertEquals("c", fullQuerySegments[3]);
        assertEquals("d", fullQuerySegments[4]);
    }

    @Test
    void should_get_by_path() {
        DemoParent parent = prepareParentObject();
        List<Object> obj = PrivateAccessor.invokeStatic(OmniAccessor.class, "getByPath", parent, "/c{DemoChild}/gc{DemoGrandChild}", "c/gc");
        assertTrue(obj.get(0) instanceof DemoGrandChild);
        assertEquals(0, ((DemoGrandChild)obj.get(0)).get());
        PrivateAccessor.set(parent.c, "gcs", new DemoGrandChild[] { new DemoGrandChild(4), new DemoGrandChild(6) });
        obj = PrivateAccessor.invokeStatic(OmniAccessor.class, "getByPath", parent, "/c{DemoChild}/gcs{DemoGrandChild[]}", "c/gcs");
        assertTrue(obj.get(0) instanceof DemoGrandChild[]);
        assertEquals(2, ((DemoGrandChild[])obj.get(0)).length);
        obj = PrivateAccessor.invokeStatic(OmniAccessor.class, "getByPath", parent, "/c{DemoChild}/gcs{DemoGrandChild[]}", "c/gcs[1]");
        assertTrue(obj.get(0) instanceof DemoGrandChild);
        assertEquals(6, ((DemoGrandChild)obj.get(0)).get());
        parent.cs = new DemoChild[] { null, prepareChildObject() };
        obj = PrivateAccessor.invokeStatic(OmniAccessor.class, "getByPath", parent, "/cs{DemoChild[]}/gcs{DemoGrandChild[]}/i{int}", "c[1]/gcs[1]/i");
        assertEquals(3, obj.get(0));
    }

    @Test
    void should_set_by_path_segment() {
        DemoParent parent = prepareParentObject();
        DemoChild child = prepareChildObject();
        PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "setByPathSegment", parent.c, "gc{DemoGrandChild}", "gc", new DemoGrandChild(2));
        assertEquals(2, parent.c.gc.get());
        PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "setByPathSegment", parent, "cs{DemoChild[]}", "cs[2]", child);
        assertNull(parent.cs[0]);
        assertNull(parent.cs[1]);
        assertEquals(5, parent.cs[2].gc.get());
        PrivateAccessor.<String>invokeStatic(OmniAccessor.class, "setByPathSegment", parent, "cs{DemoChild[]}", "cs", child);
        assertEquals(5, parent.cs[0].gc.get());
        assertEquals(5, parent.cs[1].gc.get());
        assertEquals(5, parent.cs[2].gc.get());
    }

    private DemoParent prepareParentObject() {
        DemoParent parent = OmniConstructor.newInstance(DemoParent.class);
        parent.cs = new DemoChild[3];
        return parent;
    }

    private DemoChild prepareChildObject() {
        DemoChild child = OmniConstructor.newInstance(DemoChild.class);
        PrivateAccessor.set(child, "gcs", new DemoGrandChild[] { null, new DemoGrandChild(3) });
        child.gc.set(5);
        return child;
    }

}
