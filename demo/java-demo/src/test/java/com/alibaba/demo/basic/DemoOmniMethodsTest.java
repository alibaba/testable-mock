package com.alibaba.demo.basic;

import com.alibaba.demo.basic.model.omni.Child;
import com.alibaba.demo.basic.model.omni.EnumChild;
import com.alibaba.demo.basic.model.omni.GrandChild;
import com.alibaba.demo.basic.model.omni.Parent;
import com.alibaba.testable.core.tool.OmniAccessor;
import com.alibaba.testable.core.tool.OmniConstructor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 演示快速创建任意对象和使用路径访问成员
 * Demonstrate quick object construction and access members by path
 */
class DemoOmniMethodsTest {

    @Test
    void should_construct_any_class() {
        // 所有基础类型初始化为默认数值
        GrandChild aGrandChild = OmniConstructor.newInstance(GrandChild.class);
        assertEquals(0, aGrandChild.getValue());

        // 所有枚举类型初始化为第一个可选值
        Child aChild = OmniConstructor.newInstance(Child.class);
        assertEquals(EnumChild.VAL1, aChild.getEnumChild());

        // 所有数组类型初始化为空数组
        // 所有子孙成员对象都会逐级初始化
        Parent aParent = OmniConstructor.newInstance(Parent.class);
        assertEquals(0, aParent.getChildren().length);
        assertEquals(0, aParent.getSubChild().getGrandChild().getValue());
    }

    @Test
    void should_get_any_member() {
        Parent demo = OmniConstructor.newInstance(Parent.class);

        demo.getChild().setEnumChild(EnumChild.VAL2);
        assertEquals(EnumChild.VAL2, OmniAccessor.getFirst(demo, "ec"));
        assertEquals(EnumChild.VAL2, OmniAccessor.getFirst(demo, "{EnumChild}"));
        assertEquals(EnumChild.VAL2, OmniAccessor.getFirst(demo, "c/ec"));

        demo.setChildren(new Child[]{ OmniConstructor.newInstance(Child.class) });
        assertEquals(EnumChild.VAL1, OmniAccessor.getFirst(demo, "cs[0]/ec"));
    }

    @Test
    void should_set_any_member() {

    }

}
