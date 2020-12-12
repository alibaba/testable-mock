快速Mock被测类的任意方法调用
---

相比以往Mock工具以类为粒度的Mock方式，`TestableMock`允许用户直接定义需要Mock的单个方法，并遵循约定优于配置的原则，按照规则自动在测试运行时替换被测方法中的指定方法调用。

具体的Mock方法定义约定如下：

#### 1. 覆写任意类的方法调用

在测试类里定义一个有`@MockMethod`注解的普通方法，使它与需覆写的方法名称、参数、返回值类型完全一致，然后在其参数列表首位再增加一个类型为该方法原本所属对象类型的参数。

此时被测类中所有对该需覆写方法的调用，将在单元测试运行时，将自动被替换为对上述自定义Mock方法的调用。

**注意**：当遇到待覆写方法有重名时，可以将需覆写的方法名写到`@MockMethod`注解的`targetMethod`参数里，这样Mock方法自身就可以随意命名了。

例如，被测类中有一处`"anything".substring(1, 2)`调用，我们希望在运行测试的时候将它换成一个固定字符串，则只需在测试类定义如下方法：

```java
// 原方法签名为`String substring(int, int)`
// 调用此方法的对象`"anything"`类型为`String`
// 则Mock方法签名在其参数列表首位增加一个类型为`String`的参数（名字随意）
// 此参数可用于获得当时的实际调用者的值和上下文
@MockMethod
private String substring(String self, int i, int j) {
    return "sub_string";
}
```

下面这个例子展示了`targetMethod`参数的用法，其效果与上述示例相同：

```java
// 使用`targetMethod`指定需Mock的方法名
// 此方法本身现在可以随意命名，但方法参数依然需要遵循相同的匹配规则
@MockMethod(targetMethod = "substring")
private String use_any_mock_method_name(String self, int i, int j) {
    return "sub_string";
}
```

完整代码示例见`java-demo`和`kotlin-demo`示例项目中的`should_able_to_mock_common_method()`测试用例。(由于Kotlin对String类型进行了魔改，故Kotlin示例中将被测方法在`BlackBox`类里加了一层封装)

#### 2. 覆写被测类自身的成员方法

有时候，在对某些方法进行测试时，希望将被测类自身的另外一些成员方法Mock掉。

操作方法与前一种情况相同，Mock方法的第一个参数类型需与被测类相同，即可实现对被测类自身（不论是公有或私有）成员方法的覆写。

例如，被测类中有一个签名为`String innerFunc(String)`的私有方法，我们希望在测试的时候将它替换掉，则只需在测试类定义如下方法：

```java
// 被测类型是`DemoMock`
// 因此在定义Mock方法时，在目标方法参数首位加一个类型为`DemoMock`的参数（名字随意）
@MockMethod
private String innerFunc(DemoMock self, String text) {
    return "mock_" + text;
}
```

完整代码示例见`java-demo`和`kotlin-demo`示例项目中的`should_able_to_mock_member_method()`测试用例。

#### 3. 覆写任意类的静态方法

对于静态方法的Mock与普通方法相同。但需要注意的是，静态方法的Mock方法被调用时，传入的第一个参数实际值始终是`null`。

例如，在被测类中调用了`BlackBox`类型中的静态方法`secretBox()`，改方法签名为`BlackBox secretBox()`，则Mock方法如下：

```java
// 目标静态方法定义在`BlackBox`类型中
// 在定义Mock方法时，在目标方法参数首位加一个类型为`BlackBox`的参数（名字随意）
// 此参数仅用于标识目标类型，实际传入值将始终为`null`
@MockMethod
private BlackBox secretBox(BlackBox ignore) {
    return new BlackBox("not_secret_box");
}
```

完整代码示例见`java-demo`和`kotlin-demo`示例项目中的`should_able_to_mock_static_method()`测试用例。

#### 4. 覆写任意类的new操作

在测试类里定义一个有`@MockContructor`注解的普通方法，使该方法返回值类型为要被创建的对象类型，且方法参数与要Mock的构造函数参数完全一致，方法名称随意。

此时被测类中所有用`new`创建指定类的操作（并使用了与Mock方法参数一致的构造函数）将被替换为对该自定义方法的调用。

例如，在被测类中有一处`new BlackBox("something")`调用，希望在测试时将它换掉（通常是换成Mock对象，或换成使用测试参数创建的临时对象），则只需定义如下Mock方法：

```java
// 要覆写的构造函数签名为`BlackBox(String)`
// 无需在Mock方法参数列表增加额外参数，Mock方法的名称随意起
@MockContructor
private BlackBox createBlackBox(String text) {
    return new BlackBox("mock_" + text);
}
```

> 也可以依然使用`@MockMethod`注解，并配置`targetMethod`参数值为`"<init>"`，其余同上。效果与使用`@MockContructor`注解相同

完整代码示例见`java-demo`和`kotlin-demo`示例项目中的`should_able_to_mock_new_object()`测试用例。

#### 5. 识别当前测试用例和调用来源

在Mock方法中可以通过`TestableTool.TEST_CASE`和`TestableTool.SOURCE_METHOD`来识别**当前运行的测试用例名称**和**进入该Mock方法前的被测类方法名称**，从而区分处理不同的调用场景。

完整代码示例见`java-demo`和`kotlin-demo`示例项目中的`should_able_to_get_source_method_name()`和`should_able_to_get_test_case_name()`测试用例。

#### 6. 验证Mock方法被调用的顺序和参数

在测试用例中可用通过`TestableTool.verify()`方法，配合`with()`、`withInOrder()`、`without()`、`withTimes()`等方法实现对Mock调用情况的验证。

详见[校验Mock调用](zh-cn/doc/matcher.md)文档。
