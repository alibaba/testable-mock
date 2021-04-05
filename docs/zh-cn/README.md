TestableMock简介
---

单元测试中的Mock方法，通常是为了绕开那些依赖外部资源或无关功能的方法调用，使得测试重点能够集中在需要验证和保障的代码逻辑上。

在定义Mock方法时，开发者真正关心的只有一件事："<u>这个调用，在测试的时候要换成那个假的Mock方法</u>"。

当下主流的Mock框架在实现Mock功能时，需要开发者操心的事情实在太多：Mock框架如何初始化、与所用的服务框架是否兼容、要被Mock的方法是不是私有的、是不是静态的、被Mock对象是new出来的还是注入的、怎样把被测对象送回被测类里...这些非关键的额外工作极大分散了使用Mock工具应有的乐趣。

于是，我们开发了`TestableMock`，**一款特立独行的轻量Mock工具**。

![mock](https://testable-code.oss-cn-beijing.aliyuncs.com/mock-simpson.png)
