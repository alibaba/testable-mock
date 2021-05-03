常见使用问题
---

#### 1. 如何初始化被测类中通过`@Autowired`或`@Resource`注入的私有字段？

若该对象的方法在测试时需要被Mock，则无需初始化。

若测试运行时需用到该对象的真实调用，则可以在测试类的构造方法内直接创建对象，然后利用`TestableMock`访问私有成员的能力给这些字段赋值。

对于JUnit框架，还可以使用`@RunWith(SpringRunner.class)`注解将Spring上下文启动起来，然后在测试类里用`@Resource`把需要依赖对象注入进来，再利用`PrivateAccessor`赋值给被测类的私有字段。

#### 2. `TestableMock`是否能够与其他Mock工具一起使用？

`TestableMock`可与其他基于动态代理机制的Mock工具安全的共同使用，譬如`Mockito`、`Spock`、`EasyMock`等皆属此范畴。

对于会修改类加载器或被测类字节码的Mock工具，譬如`PowerMock`和`JMockit`，尚无案例证明会与`TestableMock`发生冲突，但从原理来说二者可能存在不兼容风险，请谨慎使用。

#### 3. `TestableMock`支持哪些测试框架？

`TestableMock`的`PrivateAccessor`/`OmniConstructor`/`OmniAccessor`以及基础Mock功能均与测试框架无关，理论上适用于任何测试框架。

唯独Mock方法的调用校验器是与测试框架相关的，目前已支持`JUnit 4`/`JUnit 5`/`TestNG`/`Spock`四款主流框架。

若亲遇到在特定测试框架下的兼容问题，或希望增加其他框架的支持，请通过[Issues](https://github.com/alibaba/testable-mock/issues)告诉我们。

#### 4. 父类变量指向子类对象时，如何实现Mock方法？

在代码中，经常会有使用<u>接口变量或父类变量</u>指向子类实例，调用父类或子类方法的情况。

这时候遵循一个原则，Mock方法的首个参数类型**始终与发起调用的变量类型一致**。

因此，不论实际被调用方法来自父类还是子类，也不论子类是否覆写该方法。若发起调用的变量为父类型（或接口类型），则Mock方法的首个参数类型都应该使用相应的父类（或接口）类型。

参见Java和Kotlin示例中`DemoInheritTest`测试类的用例。

#### 5. 如何Mock对于泛型方法(模板方法)？

与普通方法的Mock方法相同，直接在Mock方法上使用相同的泛型参数即可。 

参见Java和Kotlin示例中`DemoTemplateTest`测试类的用例。

> 由于JVM存在泛型擦除机制，对于Java项目也可以直接使用`Object`类型替代泛型参数，见Java版`DemoTemplateTest`测试类中被注释掉的"第二种写法"示例。

#### 6. 如何Mock在内部类代码里的调用？

在其所在外部类对应的Mock容器中定义所需的Mock方法即可。

参见Java和Kotlin示例中`DemoInnerClass`测试类的用例。

#### 7. 在Kotlin项目对`String`类中的方法进行Mock不生效？

Kotlin语言中的`String`类型实际上是`kotlin.String`，而非`java.lang.String`。但在构建生成自字节码的时候又会被替换为Java的`java.lang.String`类，因此无论将Mock目标写为`kotlin.String`或`java.lang.String`均无法正常匹配到原始的被调用方法。

实际场景中需要对`String`类中的方法进行Mock的场景很少，`TestableMock`暂未对这种情况做特别处理。

#### 8. `TestableMock`能否用于Android项目的测试？

可以，见`demo/android-demo`示例。

需注意的是，Android系统的`Dalvik`和`ART`虚拟机采用了与标准JVM不同的字节码体系，会影响`TestableMock`的正常工作。若测试中涉及Android SDK中的类型，请结合[Roboelectric](https://github.com/robolectric/robolectric)测试框架使用，该框架能在普通JVM虚拟机上运行Android单元测试，且速度比通过Android虚拟机运行单元测试快非常多，目前许多Android App的单元测试都在使用`Roboelectric`框架。

#### 9. 在IntelliJ运行测试报"Command Line is too Long. Shorten command line for ..."错误？

这个问题是由于系统ClassPath包含太多路径所致，与是否使用`TestableMock`无关。但需要注意的是，IntelliJ提供了两种辅助解决机制：`JAR manifest`和`classpath file`，若测试中使用了`TestableMock`，请选择`JAR manifest`。

![jar-manifest](https://testable-code.oss-cn-beijing.aliyuncs.com/jar-manifest.png)
