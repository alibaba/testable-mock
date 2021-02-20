常见使用问题
---

#### 1. 如何Mock被测类中通过`@Autowired`初始化的字段？

直接创建被测类对象，然后利用`TestableMock`访问私有成员的能力直接给这些字段赋值即可。

#### 2. `TestableMock`是否能够与其他Mock工具一起使用？

`TestableMock`可与其他基于动态代理机制的Mock工具安全的共同使用，譬如`Mockito`、`Spock`、`EasyMock`等皆属此范畴。

对于会修改类加载器或被测类字节码的Mock工具，譬如`PowerMock`和`JMockit`，尚无案例证明会与`TestableMock`发生冲突，但从原理来说二者可能存在不兼容风险，请谨慎使用。

#### 3. 父类变量指向子类对象时，如何实现Mock方法？

在代码中，经常会有使用<u>接口变量或父类变量</u>指向子类实例，调用父类或子类方法的情况。

这时候遵循一个原则，Mock方法的首个参数类型**始终与发起调用的变量类型一致**。

因此，不论实际被调用方法来自父类还是子类，也不论子类是否覆写该方法。若发起调用的变量为父类型（或接口类型），则Mock方法的首个参数类型都应该使用相应的父类（或接口）类型。

参见Java和Kotlin示例中`DemoInheritTest`测试类的用例。

#### 4. 如何Mock对于泛型方法(模板方法)？

与普通方法的Mock方法相同，直接在Mock方法上使用相同的泛型参数即可。 

参见Java和Kotlin示例中`DemoTemplateTest`测试类的用例。

> 由于JVM存在泛型擦除机制，对于Java项目也可以直接使用`Object`类型替代泛型参数，见Java版`DemoTemplateTest`测试类中被注释掉的"第二种写法"示例。

#### 5. 如何Mock没有测试类的代码？

在规范的单元测试中，通常不推荐做跨单元（跨类）的测试用例，即在`A`类型的测试中应当只关注自己类的代码逻辑，若其中调用了`B`类型的某些复杂方法，则应该Mock掉，让这些逻辑在`B`类型的单元测试里验证。这也是`TestableMock`设计时遵循的一条顶层逻辑。不过在实际的单元测试中，从实用性出发，其实经常出现一次把多个单元的方法放在一个单元测试里串起来测的情况。这样一来，在使用`TestableMock`的时候就可能会遇到“要Mock的代码没有测试类”的情况。

对应的解决方法是，在测试目录的相同包路径下定义一个名称是`被测类+Mock`的类型，在其中定义Mock方法；或者在**被测类**上使用`@MockWith`注解，然后就近定义一个Mock容器类（比如直接在被测类里增加一个内部静态类）。

#### 6. 为何当测试类名不为“被测类+Test”时，要在被测类上使用`@MockWith`，而不在测试类上直接指定被测类？

从原则上来说，凡是能只改测试类就实现的，肯定不应该为了测试而去动业务代码（被测类）。

然而由于JavaAgent只能在类首次加载进内存的时候对类进行处理，实际情况并不能保证被测的类一定是在测试类之后才加载（可能在其他测试用例执行的时候就被提前加载进内存了），等读取到测试类上的信息时，已经无法对被测类进行Mock处理。因此对于测试类和被测类相互不知道对方位置的情况，采用了两边都用`@MockWith`指定Mock容器类的折中设计。

#### 7. 在Kotlin项目对`String`类中的方法进行Mock不生效？

Kotlin语言中的`String`类型实际上是`kotlin.String`，而非`java.lang.String`。但在构建生成自字节码的时候又会被替换为Java的`java.lang.String`类，因此无论将Mock目标写为`kotlin.String`或`java.lang.String`均无法正常匹配到原始的被调用方法。

实际场景中需要对`String`类中的方法进行Mock的场景很少，`TestableMock`暂未对这种情况做特别处理。

#### 8. `TestableMock`能否用于Android项目的测试？

结合[Roboelectric](https://github.com/robolectric/robolectric)测试框架可使用。

Android系统的`Dalvik`和`ART`虚拟机采用了与标准JVM不同的字节码体系，会影响`TestableMock`的正常工作。`Roboelectric`框架能在普通JVM虚拟机上运行Android单元测试，其速度比通过Android虚拟机运行单元测试快非常多，绝大多数Android App的单元测试都在使用`Roboelectric`框架。

#### 9. 在IntelliJ运行测试报"Command Line is too Long. Shorten command line for ..."错误？

这个问题是由于系统ClassPath包含太多路径所致，与是否使用`TestableMock`无关。但需要注意的是，IntelliJ提供了两种辅助解决机制：`JAR manifest`和`classpath file`，若测试中使用了`TestableMock`，请选择`JAR manifest`。

![jar-manifest](https://testable-code.oss-cn-beijing.aliyuncs.com/jar-manifest.png)
