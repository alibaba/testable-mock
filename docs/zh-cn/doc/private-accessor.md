访问私有成员字段和方法
---

如今关于私有方法是否应该做单元测试的争论正逐渐消停，开发者的普遍实践已经给出事实答案。通过公有方法间接测私有方法在很多情况下难以进行，开发者们更愿意通过修改方法可见性的办法来让原本私有的方法在测试用例中变得可测。

此外，在单元测试中时常会需要对被测对象进行特定的成员字段初始化，但有时由于被测类的构造方法限制，使得无法便捷的对这些字段进行赋值。那么，能否在不破坏被测类型封装的情况下，允许单元测试用例内的代码直接访问被测类的私有方法和成员字段呢？`TestableMock`提供了两种简单的解决方案。

### 方法一：使用`@EnablePrivateAccess`注解

只需为测试类添加`@EnablePrivateAccess`注解，即可在测试用例中获得以下增强能力：

- 调用被测类的私有方法（包括静态方法）
- 读取被测类的私有字段（包括静态字段）
- 修改被测类的私有字段（包括静态字段）
- 修改被测类的常量字段（使用final修饰的字段，包括静态字段）

访问和修改私有、常量成员时，IDE可能会提示语法有误，但编译器将能够正常运行测试。（使用编译期代码增强，目前仅实现了Java语言的适配）

效果见`java-demo`示例项目`DemoPrivateAccessTest`测试类中的用例。

> 此功能默认假设测试类与被测类同包，且名称为`被测类+Test`。当不符合此约定时，可在测试类的`@EnablePrivateAccess`注解上使用`srcClass`参数指定实际的被测类。例如：
> 
> ```java
> @EnablePrivateAccess(srcClass = DemoServiceImpl.class)
> class DemoServiceTest() { ... }
> ```

### 方法二：使用`PrivateAccessor`工具类

若不希望看到IDE的语法错误提醒，或是在非Java语言的JVM工程（譬如Kotlin语言）里，也可以借助`PrivateAccessor`工具类来直接访问私有成员。

这个类提供了6个静态方法：

- `PrivateAccessor.get(被测对象, "私有字段名")` ➜ 读取被测类的私有字段
- `PrivateAccessor.set(被测对象, "私有字段名", 新的值)` ➜ 修改被测类的私有字段（或常量字段）
- `PrivateAccessor.invoke(被测对象, "私有方法名", 调用参数..)` ➜ 调用被测类的私有方法
- `PrivateAccessor.getStatic(被测类型, "私有静态字段名")` ➜ 读取被测类的**静态**私有字段
- `PrivateAccessor.setStatic(被测类型, "私有静态字段名", 新的值)` ➜ 修改被测类的**静态**私有字段（或**静态**常量字段）
- `PrivateAccessor.invokeStatic(被测类型, "私有静态方法名", 调用参数..)` ➜ 调用被测类的**静态**私有方法

使用`PrivateAccessor`工具类并不需要测试类具有`@EnablePrivateAccess`注解，但加上此注解将开启被测类私有成员的编译期校验功能，通常建议搭配使用。

详见`java-demo`和`kotlin-demo`示例项目`DemoPrivateAccessTest`测试类中的用例。

### 私有成员编译期校验

以上两种方式本质上都是利用JVM的反射机制实现了私有成员访问，JVM编译器不会检查反射目标的存在性。当代码重构时，如果对源类型中的私有方法名称、参数进行了修改，可能导致在单元测试运行时出现较不直观的异常错误。为此，`TestableMock`对访问的私有目标提供了额外的编译期校验。

编译期校验功能通过`@EnablePrivateAccess`注解开启，对使用`方法一`访问的私有成员的情况默认生效，而对通过`方法二`访问私有成员的情况默认关闭（若要启用，给测试类加上`@EnablePrivateAccess`注解即可）。

> `@EnablePrivateAccess`注解的编译期校验功能可以手工关闭，只需将注释的`verifyTargetOnCompile`参数设为`false`。
