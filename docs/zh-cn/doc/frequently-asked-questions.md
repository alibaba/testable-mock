常见使用问题
---

#### 1. 如何Mock被测类中通过`@Autowired`初始化的字段？

直接创建被测类对象，然后利用`TestableMock`访问私有成员的能力直接给这些字段赋值即可。

#### 2. `TestableMock`是否能够与其他Mock工具一起使用？

`TestableMock`可与其他基于动态代理机制的Mock工具安全的共同使用，譬如`Mockito`、`EasyMock`、`MockRunner`等皆属此范畴。

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

#### 5. 在Kotlin项目对`String`类中的方法进行Mock不生效？

Kotlin语言中的`String`类型实际上是`kotlin.String`，而非`java.lang.String`。但在构建生成自字节码的时候又会被替换为Java的`java.lang.String`类，因此无论将Mock目标写为`kotlin.String`或`java.lang.String`均无法正常匹配到原始的被调用方法。

实际场景中需要对`String`类中的方法进行Mock的场景很少，`TestableMock`暂未对这种情况做特别处理。

#### 6. 在IntelliJ IDE 2020.3版本中运行单个测试用例时，用了`@EnablePrivateAccess`注解还是报私有成员访问错误？

IntelliJ从`2020.2.2`版本以后，IntelliJ对`JSR-269`规范注解处理器的处理机制发生了变化，与Maven标准不再完全兼容。可通过IntelliJ系统配置的"Build Tools > Maven > Runner"中开启"Delegate IDE build/run actions to maven"选项解决：

![delegate-ide-build-to-maven](https://testable-code.oss-cn-beijing.aliyuncs.com/delegate-ide-build-to-maven.png)

#### 7. `TestableMock`能否用于Android项目的测试？

结合[Roboelectric](https://github.com/robolectric/robolectric)测试框架可使用。

Android系统的`Dalvik`和`ART`虚拟机采用了与标准JVM不同的字节码体系，会影响`TestableMock`的正常工作。`Roboelectric`框架能在普通JVM虚拟机上运行Android单元测试，其速度比通过Android虚拟机运行单元测试快非常多，绝大多数Android App的单元测试都在使用`Roboelectric`框架。
