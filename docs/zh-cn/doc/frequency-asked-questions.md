常见使用问题
---

#### 1. 如何Mock被测类中通过`@Autowired`初始化的字段？

直接创建被测类对象，然后利用`TestableMock`访问私有成员的能力直接给这些字段赋值即可。

#### 2. 父类变量指向子类对象时，如何实现Mock方法？

在代码中，经常会有使用<u>接口变量或父类变量</u>指向子类实例，调用父类或子类方法的情况。

这时候遵循一个原则，Mock方法的首个参数类型**始终与发起调用的变量类型一致**。

因此，不论被调用方法来自父类还是子类，也不论子类是否覆写该方法，Mock方法的首个参数类型都应该使用变量自身的接口或父类类型。

参见Java和Kotlin示例中`DemoInheritTest`测试类的用例。

#### 3. 在Kotlin项目对`String`类中的方法进行Mock不生效？

Kotlin语言中的`String`类型实际上是`kotlin.String`，而非`java.lang.String`。但在构建生成自字节码的时候又会被替换为Java的`java.lang.String`类，因此无论将Mock目标写为`kotlin.String`或`java.lang.String`均无法正常匹配到原始的被调用方法。

实际场景中需要对`String`类中的方法进行Mock的场景很少，`TestableMock`暂未对这种情况做特别处理。

#### 4. `TestableMock`能否用于Android项目的测试？

结合[Roboelectric](https://github.com/robolectric/robolectric)测试框架可使用。

Android系统的`Dalvik`和`ART`虚拟机采用了与标准JVM不同的字节码体系，会影响`TestableMock`的正常工作。`Roboelectric`框架能在普通JVM虚拟机上运行Android单元测试，其速度比通过Android虚拟机运行单元测试快非常多，绝大多数Android App的单元测试都在使用`Roboelectric`框架。
