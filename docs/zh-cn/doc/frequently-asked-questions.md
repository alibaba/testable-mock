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

#### 5. 在Kotlin项目对`String`类中的方法进行Mock不生效？

Kotlin语言中的`String`类型实际上是`kotlin.String`，而非`java.lang.String`。但在构建生成自字节码的时候又会被替换为Java的`java.lang.String`类，因此无论将Mock目标写为`kotlin.String`或`java.lang.String`均无法正常匹配到原始的被调用方法。

实际场景中需要对`String`类中的方法进行Mock的场景很少，`TestableMock`暂未对这种情况做特别处理。

#### 6. 当被Mock的方法被其它测试类**间接调用**时依然有效吗？

同样有效，Mock的作用范围是整个测试运行过程。

例如测试类`AaaTest`中Mock了`Aaa`类的某些私有方法（或者某些外部方法调用）；在另一个测试类`BbbTest`中测试`Bbb`类时，某些方法间接用到了`Aaa`类被Mock过的方法或调用，此时实际调用的同样会是`AaaTest`类中定义的Mock方法。

#### 7. `TestableMock`能否用于Android项目的测试？

结合[Roboelectric](https://github.com/robolectric/robolectric)测试框架可使用。

Android系统的`Dalvik`和`ART`虚拟机采用了与标准JVM不同的字节码体系，会影响`TestableMock`的正常工作。`Roboelectric`框架能在普通JVM虚拟机上运行Android单元测试，其速度比通过Android虚拟机运行单元测试快非常多，绝大多数Android App的单元测试都在使用`Roboelectric`框架。

#### 8. 使用Mock时候遇到"Attempt to access none-static member in mock method"错误？

当前`TestableMock`的设计不允许在Mock方法中访问测试类的非`static`成员（因为Mock方法自身会在运行期被动态修改为`static`类型）。然而有些Java语句，包括构造块（譬如`new ArrayList<String>() {{ append("data"); }}`）、匿名函数（譬如`list.stream().map(i -> i.get)`）等等，会在编译过程中生成额外的成员方法调用，导致Mock方法执行报错。

最简单的解决办法是将Mock方法本身也声明为`static`类型（这样动态生成的调用也会是`static`的，避免了以上错误），例如原方法定义为：

```java
@MockMethod
private int getXxx(Demo self) {}
```

将其修改为：

```java
@MockMethod
private static int getXxx(Demo self) {}
```

在下一个大迭代版本（**即`0.5`版本**）中，将会在保持当前Mock体验的前提下，对Mock的实现机制进行修改，不再需要修改Mock方法为静态方法，从而彻底解决此类报错问题。

#### 9. 在IntelliJ运行测试报"Command Line is too Long. Shorten command line for ..."错误？

这个问题是由于系统ClassPath包含太多路径所致，与是否使用`TestableMock`无关。但需要注意的是，IntelliJ提供了两种辅助解决机制：`JAR manifest`和`classpath file`，若测试中使用了`TestableMock`，请选择`JAR manifest`。

![jar-manifest](https://testable-code.oss-cn-beijing.aliyuncs.com/jar-manifest.png)
