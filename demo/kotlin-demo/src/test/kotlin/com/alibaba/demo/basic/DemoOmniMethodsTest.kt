package com.alibaba.demo.basic

import com.alibaba.demo.basic.model.omni.Child
import com.alibaba.demo.basic.model.omni.Parent
import com.alibaba.testable.core.tool.OmniAccessor
import com.alibaba.testable.core.tool.OmniConstructor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * 演示快速创建任意对象和使用路径访问成员
 * Demonstrate quick object construction and access members by path
 */
internal class DemoOmniMethodsTest {

    @Test
    fun should_construct_any_class() {
        val parent = OmniConstructor.newInstance(Parent::class.java)

        // 任意深度的子孙成员对象都不为空
        assertNotNull(parent.child?.grandChild?.content)

        // 所有基础类型初始化为默认数值
        assertEquals(0, parent.child?.grandChild?.value)
        assertEquals("", parent.child?.grandChild?.content)

        // 所有数组类型初始化为空数组
        assertEquals(0, parent.children?.size)
    }

    @Test
    fun should_get_any_member() {
        val parent = OmniConstructor.newInstance(Parent::class.java)
        parent.children = OmniConstructor.newArray(Child::class.java, 3)
        parent.child?.grandChild?.content = "from child"
        parent.children?.get(0)?.grandChild?.content = "from 1st children"
        parent.children?.get(1)?.grandChild?.content = "from 2nd children"
        parent.children?.get(2)?.grandChild?.content = "from 3rd children"

        // 使用成员名称快速读取成员对象
        var contents = OmniAccessor.get<String?>(parent, "content")
        assertEquals(4, contents.size)
        assertEquals("from child", contents[0])
        assertEquals("from 1st children", contents[1])
        assertEquals("from 2nd children", contents[2])
        assertEquals("from 3rd children", contents[3])

        // 使用成员类型快速读取成员对象
        contents = OmniAccessor.get(parent, "{Child}/{GrandChild}/content")
        assertEquals(1, contents.size)
        assertEquals("from child", contents[0])

        // 使用带下标的路径读取成员对象
        assertEquals("from 2nd children", OmniAccessor.getFirst(parent, "children[1]/{GrandChild}/content"))
        assertEquals("from 3rd children", OmniAccessor.getFirst(parent, "{Child[]}[2]/{GrandChild}/content"))

        // 使用模糊路径快速读取成员对象
        assertEquals("from 1st children", OmniAccessor.getFirst(parent, "{C*[]}[0]/*/con*t"))
    }

    @Test
    fun should_set_any_member() {
        val parent = OmniConstructor.newInstance(Parent::class.java)
        parent.children = OmniConstructor.newArray(Child::class.java, 3)

        // 使用指定路径快速给成员对象赋值
        OmniAccessor.set(parent, "child/grandChild/content", "demo child")
        assertEquals("demo child", parent.child?.grandChild?.content)

        // 使用带下标的路径给成员对象赋值
        OmniAccessor.set(parent, "children[1]/grandChild/content", "demo children[1]")
        assertEquals("demo children[1]", parent.children?.get(1)?.grandChild?.content)

        // 使用模糊路径批量给成员对象赋值
        OmniAccessor.set(parent, "child*/*/content", "demo in batch")
        assertEquals("demo in batch", parent.child?.grandChild?.content)
        assertEquals("demo in batch", parent.children?.get(0)?.grandChild?.content)
        assertEquals("demo in batch", parent.children?.get(1)?.grandChild?.content)
        assertEquals("demo in batch", parent.children?.get(2)?.grandChild?.content)

        // 读写私有内部类类型的成员（使用类型名引用内部类时，无需带外部类名）
        assertEquals("", OmniAccessor.getFirst(parent, "subChild/secret"))
        OmniAccessor.set(parent, "{InnerChild}/secret", "inner-class secret")
        assertEquals("inner-class secret", OmniAccessor.getFirst(parent, "subChild/secret"))
    }
}
