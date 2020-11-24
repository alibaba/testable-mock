访问私有成员字段和方法
---

只需为测试类添加`@EnablePrivateAccess`注解，即可在测试用例中获得以下增强能力：

- 调用被测类的私有方法
- 读取被测类的私有成员
- 修改被测类的私有成员
- 修改被测类的常量成员（使用final修饰的成员）

访问和修改私有、常量成员时，IDE可能会提示语法有误，但编译器将能够正常运行测试。

若不希望看到IDE的语法错误提醒，或是在非Java语言的JVM项目里（譬如Kotlin语言），也可以借助`PrivateAccessor`工具类来实现私有成员的访问。

效果见`java-demo`和`kotlin-demo`示例项目`DemoPrivateAccessTest`测试类中的用例。
