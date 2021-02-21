升级到0.5版本
---

经过近一个月的设计和开发，TestableMock的`0.5`版本终于和大家见面了。相比`0.4`版本，新版解决了此前遗留的三大历史问题：

1. <s>**Mock方法无法调用其他非静态方法**</s>。新版中的Mock方法与普通方法不再有任何差别，可以访问任意外部方法和成员变量。
2. <s>**Mock方法总是作用于整个测试生命周期**</s>。从现在开始，Mock方法支持将生效范围限定为**所属测试类里的测试用例**，不用担心跨类测试调用被意外Mock掉了。
3. <s>**需手工清理MOCK_CONTEXT且只支持类粒度的并行测试**</s>。现在每个测试用例拥有了独立的`MOCK_CONTEXT`变量，无需清理也不会串号，而且可以放心使用**任意粒度**的并行单元测试啦。

在使用方式上，`0.5`版本延续`TestableMock`简洁轻量的原则，同时为了更好的实现Mock方法复用，新版本的Mock类与测试类之间有了明确的边界。从`0.4`版本升级到`0.5`时，唯一需要的改变是将测试类中的所有Mock方法使用一个`public static class Mock { }`包裹起来。

例如，原先有如下测试类定义：

```java
public class DemoMockTest {
    @MockMethod(targetClass = DemoMock.class)
    private String innerFunc(String text) {
        return "hello_" + text;
    }
        
    @Test
    void should_able_to_mock_member_method() throws Exception {
        assertEquals("hello_world", demoMock.outerFunc());
        verify("innerFunc").with("world");
    }
}
```

升级为`0.5`版本后，将所有Mock方法（此例中只有`innerFunc`这一个方法）移到一个名称为`Mock`的静态内部类中，相当于增加两行代码：

```java
public class DemoMockTest {

    public static class Mock {   // 增加此行
        @MockMethod(targetClass = DemoMock.class)
        private String innerFunc(String text) {
            return "hello_" + text;
        }
    }                            // 增加此行
        
    @Test
    void should_able_to_mock_member_method() throws Exception {
        assertEquals("hello_world", demoMock.outerFunc());
        verify("innerFunc").with("world");
    }
}
```

然后将`pom.xml`或`build.gradle`文件中的TestableMock依赖升级到`0.5.0`或以上版本即可。
