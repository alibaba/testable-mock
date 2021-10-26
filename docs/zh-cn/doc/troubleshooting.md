自助问题排查
---

相比`Mockito`等由开发者手工放置Mock类的做法，`TestableMock`使用方法名和参数类型匹配自动寻找需Mock的调用。这种机制在带来方便的同时也容易导致对“Mock究竟有没生效”的疑问。

为此，`TestableMock`会在项目构建目录下自动保存最后一次测试运行过程的Mock扫描日志。默认位置为`target/testable-agent.log`（Maven项目）或`build/testable-agent.log`（Gradle项目）。

日志内容示例如下：

```text
[INFO] Start at Mon Jan 00 00:00:00 CST 0000
... ...
[INFO] Found test class com/alibaba/testable/demo/basic/DemoMockTest
[INFO]   Found 6 test cases
[INFO] Found mock class com/alibaba/testable/demo/basic/DemoMockTest$Mock
[INFO]   Found 8 mock methods
[INFO] Found source class com/alibaba/testable/demo/basic/DemoMock
[INFO]   Found method <init>
[INFO]   Found method newFunc
[INFO]     Line 19, mock method "createBlackBox" used
[INFO]   Found method outerFunc
[INFO]     Line 27, mock method "innerFunc" used
[INFO]     Line 27, mock method "staticFunc" used
[INFO]   Found method commonFunc
[INFO]     Line 34, mock method "trim" used
[INFO]     Line 34, mock method "sub" used
[INFO]     Line 34, mock method "startsWith" used
... ...
[INFO] Completed at Mon Jan 00 00:00:00 CST 0000
```

其中`Line XX, mock method "XXX" used`日志展示了被测类中所有发生了Mock替换的调用和相应代码行号。

依据需排查的测试类，进行针对性排查。假设被测类为"com.demo.BizService"，测试类为"com.demo.BizServiceTest"，Mock容器类为"com.demo.BizServiceTest.Mock"：

- 若该日志文件未生成，请检查`pom.xml`或`build.gradle`配置是否正确引入了`TestableMock`依赖
- 若日志中只能找到`com/demo/BizServiceTest$Mock`，没有找到被测类和测试类，请检查Mock容器类的名称和位置是否符合规范
- 若日志中找到了Mock类和测试类，但没有找到被测类`com/demo/BizService`，请检查被测类与测试类是否包路径相同，且名称为"被测类+Test"，或者是否正确的使用了`@MockWith`注解
- 若日志中三个类都已经找到，且有`Found method xxx`，但预期的代码行位置没有发生Mock替换，请继续检查Mock方法定义是否未与目标方法匹配

对于上述的最后一种情况（预期Mock未生效），可使用`@MockDiagnose`注解将相应Mock容器类的日志级别提升到`LogLevel.VERBOSE`做进一步排查。例如：

```java
class BizServiceTest {
    @MockDiagnose(LogLevel.VERBOSE)
    public static class Mock {
        ...
    }
}
```

再次执行单元测试，此时日志将会包含所有该Mock类中的方法签名定义，以及被测类中扫描到所有调用的实际方法签名：

```text
[INFO] Found test class com/alibaba/testable/demo/basic/DemoMockTest
[TIP]    Test case "should_mock_new_object"
... ...
[TIP]    Test case "should_set_mock_context"
[INFO]   Found 6 test cases
[INFO] Found mock class com/alibaba/testable/demo/basic/DemoMockTest$Mock
[TIP]    Mock constructor "createBlackBox" as "com.alibaba.demo.basic.model.mock.BlackBox(java.lang.String)"
[TIP]    Mock method "innerFunc" as "com.alibaba.demo.basic.DemoMock::innerFunc(java.lang.String) : java.lang.String"
... ...
[TIP]    Mock method "callFromDifferentMethod" as "()Ljava/lang/String;"
[INFO]   Found 8 mock methods
[INFO] Found source class com/alibaba/testable/demo/basic/DemoMock
[INFO]   Found method <init>
[TIP]      Line 13, constructing "java.lang.Object()"
[INFO]   Found method newFunc
[TIP]      Line 19, constructing "com.alibaba.demo.basic.model.mock.BlackBox(java.lang.String)"
[INFO]     Line 19, mock method "createBlackBox" used
[TIP]      Line 19, invoking "com.alibaba.demo.basic.DemoMockTest$Mock::createBlackBox(java.lang.String) : com.alibaba.demo.basic.model.mock.BlackBox"
[TIP]      Line 20, invoking "com.alibaba.demo.basic.model.mock.BlackBox::get() : java.lang.String"
[INFO]   Found method outerFunc
[TIP]      Line 27, constructing "java.lang.StringBuilder()"
[TIP]      Line 27, invoking "java.lang.StringBuilder::append(java.lang.String) : java.lang.StringBuilder"
[TIP]      Line 27, invoking "com.alibaba.demo.basic.DemoMock::innerFunc(java.lang.String) : java.lang.String"
[INFO]     Line 27, mock method "innerFunc" used
... ...
```

输出日志结构参考如下：

- `Mock constructor "<Mock方法名>" as "<方法签名>" for "<类型>"` 在Mock类中扫描到的**Mock构造方法**及其签名
- `Mock method "<Mock方法名>" as "<方法签名>"` 在Mock类中扫描到的**普通Mock方法**及其签名
- `Line XX, constructing "<类型>" as "<方法签名>"` 在被测类中扫描掉的**构造方法调用**及其签名
- `Line XX, invoking "<方法名>" as "<方法签名>"` 在被测类中扫描到的**成员方法调用**及其签名

> 为了便于清楚的区分返回值类型和调用目标类型，日志中记录的方法签名采用了类似`Kotlin`的方法定义结构。

对比原调用的实际签名和Mock方法定义的签名，通常很快就能够找出问题所在。
