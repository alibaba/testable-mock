package com.alibaba.demo.basic;

import com.alibaba.demo.basic.model.omni.Child;
import com.alibaba.demo.basic.model.omni.Parent;
import com.alibaba.testable.core.tool.OmniAccessor;
import com.alibaba.testable.core.tool.OmniConstructor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 演示快速创建任意对象和使用路径访问成员
 * Demonstrate quick object construction and access members by path
 */
class DemoOmniMethodsTest {

    @Test
    void should_construct_any_class() {
        Parent parent = OmniConstructor.newInstance(Parent.class);

        // 任意深度的子孙成员对象都不为空
        assertNotNull(parent.getChild().getGrandChild().getContent());

        // 所有基础类型初始化为默认数值
        assertEquals(0, parent.getChild().getGrandChild().getValue());
        assertEquals("", parent.getChild().getGrandChild().getContent());

        // 所有数组类型初始化为空数组
        assertEquals(0, parent.getChildren().length);
    }

    @Test
    void should_get_any_member() {
        Parent parent = OmniConstructor.newInstance(Parent.class);
        parent.setChildren(OmniConstructor.newArray(Child.class, 3));
        parent.getChild().getGrandChild().setContent("from child");
        parent.getChildren()[0].getGrandChild().setContent("from 1st children");
        parent.getChildren()[1].getGrandChild().setContent("from 2nd children");
        parent.getChildren()[2].getGrandChild().setContent("from 3rd children");

        // 使用成员名称快速读取成员对象
        List<String> contents = OmniAccessor.get(parent, "content");
        assertEquals(4, contents.size());
        assertEquals("from child", contents.get(0));
        assertEquals("from 1st children", contents.get(1));
        assertEquals("from 2nd children", contents.get(2));
        assertEquals("from 3rd children", contents.get(3));

        // 使用成员类型快速读取成员对象
        contents = OmniAccessor.get(parent, "{Child}/{GrandChild}/content");
        assertEquals(1, contents.size());
        assertEquals("from child", contents.get(0));

        // 使用带下标的路径读取成员对象
        assertEquals("from 2nd children", OmniAccessor.getFirst(parent, "children[1]/{GrandChild}/content"));
        assertEquals("from 3rd children", OmniAccessor.getFirst(parent, "{Child[]}[2]/{GrandChild}/content"));

        // 使用模糊路径快速读取成员对象
        assertEquals("from 1st children", OmniAccessor.getFirst(parent, "{C*[]}[0]/*/con*t"));
    }

    @Test
    void should_set_any_member() {
        Parent parent = OmniConstructor.newInstance(Parent.class);
        parent.setChildren(OmniConstructor.newArray(Child.class, 3));

        // 使用指定路径快速给成员对象赋值
        OmniAccessor.set(parent, "child/grandChild/content", "demo child");
        assertEquals("demo child", parent.getChild().getGrandChild().getContent());

        // 使用带下标的路径给成员对象赋值
        OmniAccessor.set(parent, "children[1]/grandChild/content", "demo children[1]");
        assertEquals("demo children[1]", parent.getChildren()[1].getGrandChild().getContent());

        // 使用模糊路径批量给成员对象赋值
        OmniAccessor.set(parent, "child*/*/content", "demo in batch");
        assertEquals("demo in batch", parent.getChild().getGrandChild().getContent());
        assertEquals("demo in batch", parent.getChildren()[0].getGrandChild().getContent());
        assertEquals("demo in batch", parent.getChildren()[1].getGrandChild().getContent());
        assertEquals("demo in batch", parent.getChildren()[2].getGrandChild().getContent());

        // 读写私有内部类类型的成员（使用类型名引用内部类时，无需带外部类名）
        assertEquals("", OmniAccessor.getFirst(parent, "subChild/secret"));
        OmniAccessor.set(parent, "{InnerChild}/secret", "inner-class secret");
        assertEquals("inner-class secret", OmniAccessor.getFirst(parent, "subChild/secret"));
    }

}
