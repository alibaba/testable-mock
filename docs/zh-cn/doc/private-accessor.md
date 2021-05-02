访问私有成员字段和方法
---

如今关于私有方法是否应该做单元测试的争论正逐渐消停，开发者的普遍实践已经给出事实答案。通过公有方法间接测私有方法在很多情况下难以进行，开发者们更愿意通过修改方法可见性的办法来让原本私有的方法在测试用例中变得可测。

此外，在单元测试中时常会需要对被测对象进行特定的成员字段初始化，但有时由于被测类的构造方法限制，使得无法便捷的对这些字段进行赋值。那么，能否在不破坏被测类型封装的情况下，允许单元测试用例内的代码直接访问被测类的私有方法和成员字段呢？`TestableMock`提供了两种简单的解决方案。

### 1.1 使用`PrivateAccessor`工具类

第一种方法是借助`PrivateAccessor`工具类来直接访问私有成员。这个类提供7个静态方法：

- `PrivateAccessor.get(任意对象, "私有字段名")` ➜ 读取任意类的私有字段
- `PrivateAccessor.set(任意对象, "私有字段名", 新的值)` ➜ 修改任意类的私有字段（或常量字段）
- `PrivateAccessor.invoke(任意对象, "私有方法名", 调用参数...)` ➜ 调用任意类的私有方法
- `PrivateAccessor.getStatic(任意类型, "私有静态字段名")` ➜ 读取任意类的**静态**私有字段
- `PrivateAccessor.setStatic(任意类型, "私有静态字段名", 新的值)` ➜ 修改任意类的**静态**私有字段（或**静态**常量字段）
- `PrivateAccessor.invokeStatic(任意类型, "私有静态方法名", 调用参数...)` ➜ 调用任意类的**静态**私有方法
- `PrivateAccessor.construct(任意类型, 构造方法参数...)` ➜ 调用任意类的私有构造方法

详见`java-demo`和`kotlin-demo`示例项目`DemoPrivateAccessorTest`测试类中的用例。

### 1.2 防代码重构机制

本质上来说，`PrivateAccessor`只是JVM反射机制的“易用型”封装，因此会存在与JVM反射相同的“对代码重构不友好”问题。当被测类中的私有方法名称、参数进行了修改，由于IDE无法自动订正反射访问的代码，往往相关错误要在单元测试运行时才能被发现。

为此，`TestableMock`对`PrivateAccessor`进行了增强，赋予其编译期私有成员校验能力。这项功能默认关闭，需要通过`@EnablePrivateAccess`注解开启。（实际上是通过该注解的`verifyTargetOnCompile`参数控制，由于此参数默认值为`true`，因此只需在被测类上添加该注解即可启用私有成员校验）

**注意 1**：当私有成员校验功能开启时，`PrivateAccessor`工具类将只能用于访问**被测类**的私有成员，从某种角度而言，这也有助于限制将`PrivateAccessor`工具类用于与当前测试无关的“越权”操作。

**注意 2**：`TestableMock`默认约定测试类与**被测类**的包路径相同，且名称为`被测类+Test`。若测试类名称不符合此约定时，在使用`@EnablePrivateAccess`注解时，需用`srcClass`参数显式指明实际的**被测类**位置。

**注意 3**： 此机制目前只针对`Java`语言实现，对于`Kotlin`以及其他JVM方言均无效。

> 将`DemoPrivateAccessorTest`示例代码稍加修改，添加`@EnablePrivateAccess`注解（注意此时测试类名不符合约定，需加`srcClass`参数）：
>
> ```java
> @EnablePrivateAccess(srcClass = DemoPrivateAccess.class)  // <- 添加此行
> class DemoPrivateAccessorTest() { ... }
> ```
>
> 然后将任意一处通过`PrivateAccessor`访问的目标名称改为实际不存在的成员名，再次编译时即可发现该行有编译错误，提示信息为访问目标不存在。

### 2.1 直接访问私有成员

> 由于IDE语法报错原因，此特性计划在未来版本中移除，建议采用`PrivateAccessor`方式

第二种方法，除了借助`PrivateAccessor`工具类以外，凡是使用了`@EnablePrivateAccess`注解的测试类还会被自动赋予以下“特殊能力”：

- 调用**被测类**的私有方法（包括静态方法）
- 读取**被测类**的私有字段（包括静态字段）
- 修改**被测类**的私有字段（包括静态字段）
- 修改**被测类**的常量字段（使用`final`修饰的字段，包括静态常量字段）

访问和修改私有、常量成员时，IDE可能会提示语法有误，但编译器将能够正常运行测试。（使用编译期代码增强，目前仅实现了`Java`语言的适配）

效果见`java-demo`示例项目`DemoPrivateProcessorTest`测试类中的用例。
