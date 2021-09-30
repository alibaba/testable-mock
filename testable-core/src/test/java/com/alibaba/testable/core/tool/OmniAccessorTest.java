package com.alibaba.testable.core.tool;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static com.alibaba.testable.core.tool.PrivateAccessor.*;
import static org.junit.jupiter.api.Assertions.*;

class OmniAccessorTest {

    @Test
    void should_generate_member_index() {
        List<String> index = invokeStatic(OmniAccessor.class, "generateMemberIndex", DemoParent.class);
        assertEquals(34, index.size());
        HashSet<String> expected = new HashSet<String>(){{
            add("/c{DemoChild}");
            add("/c{DemoChild}/gc{DemoGrandChild}");
            add("/c{DemoChild}/gc{DemoGrandChild}/i{int}");
            add("/c{DemoChild}/gc{DemoGrandChild}/l{long}");
            add("/c{DemoChild}/gc{DemoGrandChild}/si{Integer}");
            add("/c{DemoChild}/gc{DemoGrandChild}/sl{Long}");
            add("/c{DemoChild}/gcs{DemoGrandChild[]}");
            add("/c{DemoChild}/gcs{DemoGrandChild[]}/i{int}");
            add("/c{DemoChild}/gcs{DemoGrandChild[]}/l{long}");
            add("/c{DemoChild}/gcs{DemoGrandChild[]}/si{Integer}");
            add("/c{DemoChild}/gcs{DemoGrandChild[]}/sl{Long}");
            add("/cs{DemoChild[]}");
            add("/cs{DemoChild[]}/gc{DemoGrandChild}");
            add("/cs{DemoChild[]}/gc{DemoGrandChild}/i{int}");
            add("/cs{DemoChild[]}/gc{DemoGrandChild}/l{long}");
            add("/cs{DemoChild[]}/gc{DemoGrandChild}/si{Integer}");
            add("/cs{DemoChild[]}/gc{DemoGrandChild}/sl{Long}");
            add("/cs{DemoChild[]}/gcs{DemoGrandChild[]}");
            add("/cs{DemoChild[]}/gcs{DemoGrandChild[]}/i{int}");
            add("/cs{DemoChild[]}/gcs{DemoGrandChild[]}/l{long}");
            add("/cs{DemoChild[]}/gcs{DemoGrandChild[]}/si{Integer}");
            add("/cs{DemoChild[]}/gcs{DemoGrandChild[]}/sl{Long}");
            add("/sc{SubChild}");
            add("/sc{SubChild}/gc{DemoGrandChild}");
            add("/sc{SubChild}/gc{DemoGrandChild}/i{int}");
            add("/sc{SubChild}/gc{DemoGrandChild}/l{long}");
            add("/sc{SubChild}/gc{DemoGrandChild}/si{Integer}");
            add("/sc{SubChild}/gc{DemoGrandChild}/sl{Long}");
            add("/ssc{StaticSubChild}");
            add("/ssc{StaticSubChild}/gc{DemoGrandChild}");
            add("/ssc{StaticSubChild}/gc{DemoGrandChild}/i{int}");
            add("/ssc{StaticSubChild}/gc{DemoGrandChild}/l{long}");
            add("/ssc{StaticSubChild}/gc{DemoGrandChild}/si{Integer}");
            add("/ssc{StaticSubChild}/gc{DemoGrandChild}/sl{Long}");
        }};
        assertEquals(expected, new HashSet<String>(index));
    }

    @Test
    void should_to_pattern() {
        assertEquals(".*/", invokeStatic(OmniAccessor.class, "toPattern", ""));
        assertEquals(".*/abc\\{[^}]+\\}", invokeStatic(OmniAccessor.class, "toPattern", "abc"));
        assertEquals(".*/[^{]+\\{Abc\\}", invokeStatic(OmniAccessor.class, "toPattern", "{Abc}"));
        assertEquals(".*/abc\\{[^}]+\\}/xyz\\{[^}]+\\}", invokeStatic(OmniAccessor.class, "toPattern", "abc/xyz"));
        assertEquals(".*/[^{]+\\{Abc\\}/xyz\\{[^}]+\\}", invokeStatic(OmniAccessor.class, "toPattern", "{Abc}/xyz"));
        assertEquals(".*/abc\\{[^}]+\\}/[^{]+\\{Xyz\\}", invokeStatic(OmniAccessor.class, "toPattern", "abc/{Xyz}"));
        assertEquals(".*/[^{]+\\{Abc\\}/[^{]+\\{Xyz\\}", invokeStatic(OmniAccessor.class, "toPattern", "{Abc}/{Xyz}"));
        assertEquals(".*/[^{]+\\{Abc\\[\\]\\}/[^{]+\\{Xyz\\[\\]\\}", invokeStatic(OmniAccessor.class, "toPattern", "{Abc[]}/{Xyz[]}"));
        assertEquals(".*/abc\\{[^}]+\\}/[^{]+\\{Xyz\\[\\]\\}", invokeStatic(OmniAccessor.class, "toPattern", "abc[1]/{Xyz[]}[2]"));
        assertEquals(".*/abc\\{[^}]+\\}/[^{]+\\{Xyz\\}/demo\\{[^}]+\\}", invokeStatic(OmniAccessor.class, "toPattern", "abc/{Xyz}/demo"));
        assertEquals(".*/abc\\{[^}]+\\}/de[^{]*\\{[^}]+\\}/[^{]+\\{Xyz\\}", invokeStatic(OmniAccessor.class, "toPattern", "abc/de*/{Xyz}"));
        assertEquals(".*/abc\\{[^}]+\\}/[^{]*mo\\{[^}]+\\}/[^{]+\\{Xyz\\}", invokeStatic(OmniAccessor.class, "toPattern", "abc/*mo/{Xyz}"));
        assertEquals(".*/abc\\{[^}]+\\}/d[^{]*o\\{[^}]+\\}/[^{]+\\{Xyz\\}", invokeStatic(OmniAccessor.class, "toPattern", "abc/d*o/{Xyz}"));
        assertEquals(".*/abc\\{[^}]+\\}/[^{]+\\{De[^}]*\\}/[^{]+\\{Xyz\\}", invokeStatic(OmniAccessor.class, "toPattern", "abc/{De*}/{Xyz}"));
        assertEquals(".*/abc\\{[^}]+\\}/[^{]+\\{[^}]*mo\\}/[^{]+\\{Xyz\\}", invokeStatic(OmniAccessor.class, "toPattern", "abc/{*mo}/{Xyz}"));
        assertEquals(".*/abc\\{[^}]+\\}/[^{]+\\{D[^}]*o\\}/[^{]+\\{Xyz\\}", invokeStatic(OmniAccessor.class, "toPattern", "abc/{D*o}/{Xyz}"));
        assertEquals(".*/abc\\{[^}]+\\}/[^/]+/[^{]+\\{Xyz\\}/[^/]+/demo\\{[^}]+\\}", invokeStatic(OmniAccessor.class, "toPattern", "abc/*/{Xyz}/*/demo"));
    }

    @Test
    void should_to_parent() {
        assertEquals("", invokeStatic(OmniAccessor.class, "toParent", ""));
        assertEquals("", invokeStatic(OmniAccessor.class, "toParent", "abc"));
        assertEquals("abc", invokeStatic(OmniAccessor.class, "toParent", "abc/xyz"));
        assertEquals("abc/def", invokeStatic(OmniAccessor.class, "toParent", "abc/def/xyz"));
        assertEquals("/abc/def", invokeStatic(OmniAccessor.class, "toParent", "/abc/def/xyz"));
    }

    @Test
    void should_to_child() {
        assertEquals("", invokeStatic(OmniAccessor.class, "toChild", ""));
        assertEquals("abc", invokeStatic(OmniAccessor.class, "toChild", "abc"));
        assertEquals("xyz", invokeStatic(OmniAccessor.class, "toChild", "abc/xyz"));
        assertEquals("xyz", invokeStatic(OmniAccessor.class, "toChild", "abc/def/xyz"));
        assertEquals("xyz", invokeStatic(OmniAccessor.class, "toChild", "/abc/def/xyz"));
    }

    @Test
    void should_get_full_query_segments() {
        String[] querySegments = new String[] { "c", "d" };
        String[] memberSegments = new String[] { "a{A}", "b{B}", "c{C}", "d{D}" };
        String[] fullQuerySegments = invokeStatic(OmniAccessor.class, "calculateFullQueryPath", querySegments, memberSegments);
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
        List<Object> obj = invokeStatic(OmniAccessor.class, "getByPath", parent, "/c{DemoChild}/gc{DemoGrandChild}", "c/gc");
        assertTrue(obj.get(0) instanceof DemoGrandChild);
        assertEquals(1, ((DemoGrandChild)obj.get(0)).get());
        set(parent.c, "gcs", new DemoGrandChild[] { new DemoGrandChild(), new DemoGrandChild() });
        obj = invokeStatic(OmniAccessor.class, "getByPath", parent, "/c{DemoChild}/gcs{DemoGrandChild[]}", "c/gcs");
        assertTrue(obj.get(0) instanceof DemoGrandChild[]);
        assertEquals(2, ((DemoGrandChild[])obj.get(0)).length);
        obj = invokeStatic(OmniAccessor.class, "getByPath", parent, "/c{DemoChild}/gcs{DemoGrandChild[]}", "c/gcs[1]");
        assertTrue(obj.get(0) instanceof DemoGrandChild);
        assertEquals(1, ((DemoGrandChild)obj.get(0)).get());
        parent.cs = new DemoChild[] { null, prepareChildObject() };
        obj = invokeStatic(OmniAccessor.class, "getByPath", parent, "/cs{DemoChild[]}/gcs{DemoGrandChild[]}/i{int}", "c[1]/gcs[1]/i");
        assertEquals(1, obj.get(0));
    }

    @Test
    void should_set_by_path_segment() {
        DemoParent parent = prepareParentObject();
        DemoChild child = prepareChildObject();
        invokeStatic(OmniAccessor.class, "setByPathSegment", parent.c, "gc{DemoGrandChild}", "gc", new DemoGrandChild());
        assertEquals(1, parent.c.gc.get());
        invokeStatic(OmniAccessor.class, "setByPathSegment", parent, "cs{DemoChild[]}", "cs[2]", child);
        assertNull(parent.cs[0]);
        assertNull(parent.cs[1]);
        assertEquals(5, parent.cs[2].gc.get());
        invokeStatic(OmniAccessor.class, "setByPathSegment", parent, "cs{DemoChild[]}", "cs", child);
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
        set(child, "gcs", new DemoGrandChild[] { null, new DemoGrandChild() });
        child.gc.set(5);
        return child;
    }

}
