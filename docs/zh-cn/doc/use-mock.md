快速Mock被测类的任意方法调用
---

相比以往Mock工具以类为粒度的Mock方式，`TestableMock`允许用户直接定义需要Mock的单个方法，并遵循约定优于配置的原则，按照规则自动在测试运行时替换被测方法中的指定方法调用。

> 归纳起来就两条：
> - Mock非构造方法，拷贝原方法定义到测试类，加`@MockMethod`注解
> - Mock构造方法，拷贝原方法定义到测试类，返回值换成构造的类型，方法名随意，加`@MockContructor`注解

具体的Mock方法定义约定如下：

#### 1. 覆写任意类的方法调用

在测试类里定义一个有`@MockMethod`注解的普通方法，使它与需覆写的方法名称、参数、返回值类型完全一致，并在注解的`targetClass`参数指定该方法原本所属对象类型。

此时被测类中所有对该需覆写方法的调用，将在单元测试运行时，将自动被替换为对上述自定义Mock方法的调用。

例如，被测类中有一处`"anything".substring(1, 2)`调用，我们希望在运行测试的时候将它换成一个固定字符串，则只需在测试类定义如下方法：

```java
// 原方法签名为`String substring(int, int)`
// 调用此方法的对象`"anything"`类型为`String`
@MockMethod(targetClass = String.class)
private String substring(int i, int j) {
    return "sub_string";
}
```

当遇到待覆写方法有重名时，可以将需覆写的方法名写到`@MockMethod`注解的`targetMethod`参数里，这样Mock方法自身就可以随意命名了。

下面这个例子展示了`targetMethod`参数的用法，其效果与上述示例相同：

```java
// 使用`targetMethod`指定需Mock的方法名
// 此方法本身现在可以随意命名，但方法参数依然需要遵循相同的匹配规则
@MockMethod(targetClass = String.class, targetMethod = "substring")
private String use_any_mock_method_name(int i, int j) {
    return "sub_string";
}
```

有时，在Mock方法里会需要访问发起调用的原始对象中的成员变量，或是调用原始对象的其他方法。此时，可以将`@MockMethod`注解中的`targetClass`参数去除，然后在方法参数列表首位增加一个类型为该方法原本所属对象类型的参数。

`TestableMock`约定，当`@MockMethod`注解的`targetClass`参数值为空时，Mock方法的首位参数即为目标方法所属类型，参数名称随意。通常为了便于代码阅读，建议将此参数统一命名为`self`或`src`。举例如下：

```java
// Mock方法在参数列表首位增加一个类型为`String`的参数（名字随意）
// 此参数可用于获得当时的实际调用者的值和上下文
@MockMethod
private String substring(String self, int i, int j) {
    // 可以直接调用原方法，此时Mock方法仅用于记录调用，常见于对void方法的测试
    return self.substring(i, j);
}
```

完整代码示例见`java-demo`和`kotlin-demo`示例项目中的`should_able_to_mock_common_method()`测试用例。(由于Kotlin对String类型进行了魔改，故Kotlin示例中将被测方法在`BlackBox`类里加了一层封装)

#### 2. 覆写被测类自身的成员方法

有时候，在对某些方法进行测试时，希望将被测类自身的另外一些成员方法Mock掉（比如这个方法里有许多外部依赖或耗时操作）。

做法与前一种情况完全相同，只需将`targetClass`参数赋值为被测类，即可实现对被测类自身（不论是公有或私有）成员方法的覆写。

例如，被测类中有一个签名为`String innerFunc(String)`的私有方法，我们希望在测试的时候将它替换掉，则只需在测试类定义如下方法：

```java
// 被测类型是`DemoMock`
@MockMethod(targetClass = DemoMock.class)
private String innerFunc(String text) {
    return "mock_" + text;
}
```

同样的，上述示例中的方法如需访问发起调用的原始被测对象，也可不使用`targetClass`参数，而是在定义Mock方法时，在方法参数列表首位加一个类型为`DemoMock`的参数（名字随意）。

完整代码示例见`java-demo`和`kotlin-demo`示例项目中的`should_able_to_mock_member_method()`测试用例。

#### 3. 覆写任意类的静态方法

对于静态方法的Mock与普通方法相同。

例如，在被测类中调用了`BlackBox`类型中的静态方法`secretBox()`，该方法签名为`BlackBox secretBox()`，则Mock方法如下：

```java
@MockMethod(targetClass = BlackBox.class)
private BlackBox secretBox() {
    return new BlackBox("not_secret_box");
}
```

对于静态方法的Mock，通常不使用方法参数列表的首位加参数来表示目标类型。但这种方法也依然适用，只是实际传入的第一个参数值将始终是`null`。

完整代码示例见`java-demo`和`kotlin-demo`示例项目中的`should_able_to_mock_static_method()`测试用例。

#### 4. 覆写任意类的new操作

在测试类里定义一个返回值类型为要被创建的对象类型，且方法参数与要Mock的构造函数参数完全一致的方法，名称随意，然后加上`@MockContructor`注解。

此时被测类中所有用`new`创建指定类的操作（并使用了与Mock方法参数一致的构造函数）将被替换为对该自定义方法的调用。

例如，在被测类中有一处`new BlackBox("something")`调用，希望在测试时将它换掉（通常是换成Mock对象，或换成使用测试参数创建的临时对象），则只需定义如下Mock方法：

```java
// 要覆写的构造函数签名为`BlackBox(String)`
// Mock方法返回`BlackBox`类型对象，方法的名称随意起
@MockContructor
private BlackBox createBlackBox(String text) {
    return new BlackBox("mock_" + text);
}
```

完整代码示例见`java-demo`和`kotlin-demo`示例项目中的`should_able_to_mock_new_object()`测试用例。

#### 5. 识别当前测试用例和调用来源

在Mock方法中通过`TestableTool.SOURCE_METHOD`变量可以识别**进入该Mock方法前的被测类方法名称**；此外，还可以借助`TestableTool.MOCK_CONTEXT`变量为Mock方法注入“**额外的上下文参数**”，从而区分处理不同的调用场景。

例如，在测试用例中验证当被Mock方法返回不同结果时，对被测目标方法的影响：

```java
@Test
public void testDemo() {
    MOCK_CONTEXT.put("case", "data-ready");
    assertEquals(true, demo());
    MOCK_CONTEXT.put("case", "has-error");
    assertEquals(false, demo());
    MOCK_CONTEXT.clear();
}
```

在Mock方法中取出注入的参数，根据情况返回不同结果：

```java
@MockMethod
private Data mockDemo() {
    switch((String)MOCK_CONTEXT.get("case")) {
        case "data-ready":
            return new Data();
        case "has-error":
            throw new NetworkException();
        default:
            return null;
    }
}
```

注意，由于`TestableMock`并不依赖（也不希望依赖）任何特定测试框架，因而无法自动识别单个测试用例的结束位置，这使得设置到`TestableTool.MOCK_CONTEXT`变量的参数可能会在同测试类中跨测试用例存在。建议总是在使用后及时使用`MOCK_CONTEXT.clear()`清空上下文，也可将这行语句添加到单元测试框架特定的测试用例结束的统一位置，比如JUnit 5的`@AfterEach`方法。

在当前版本中，此变量在运行期的效果类似于一个在测试类中的普通`Map`类型成员对象，但请尽量使用此变量而非自定义对象传递附加的Mock参数，以便在将来升级至`v0.5`版本时获得更好的兼容性。

> `TestableTool.MOCK_CONTEXT`变量的值是在测试类内共享的，当单元测试并行运行时，建议请选择`parallel`类型为`classes`

完整代码示例见`java-demo`和`kotlin-demo`示例项目中的`should_able_to_get_source_method_name()`和`should_able_to_get_test_case_name()`测试用例。

#### 6. 验证Mock方法被调用的顺序和参数

在测试用例中可用通过`TestableTool.verify()`方法，配合`with()`、`withInOrder()`、`without()`、`withTimes()`等方法实现对Mock调用情况的验证。

详见[校验Mock调用](zh-cn/doc/matcher.md)文档。

#### 特别说明

> **0.4.x 版本的Mock约定**：
> - 测试类与被测类的包路径应相同，且名称为`被测类名+Test`（通常采用`Maven`或`Gradle`构建的Java项目均符合这种惯例）
> - Mock方法（即包含`@MockMethod`或`@MockContructor`注解的方法）会在运行期被自动修改为`static`方法，请勿在Mock方法的定义中访问任何非静态成员。
>
> 这两项约束会在`0.5`版本中去除
>
> 当Mock方法内容较复杂（包含Lambda语句、构造块、匿名类等）时，编译器会在构建期生成额外的非静态临时方法，导致"Bad type in operand stack"错误。如果有遇到此类错误，请将Mock方法显式加上`static`修饰即可解决。这个问题会在`0.5`版本中彻底解决。
