自助问题排查
---

相比`Mockito`等由开发者手工放置Mock类的做法，`TestableMock`使用方法名和参数类型匹配自动寻找需Mock的调用。这种机制在带来方便的同时也容易导致对“Mock究竟有没生效”的疑问。

若要排查Mock相关的问题，只需在相应的**Mock容器类**上添加`@MockDiagnose`注解，并配置参数值为`LogLevel.ENABLE`，在运行测试时就会打印出详细的Mock方法替换过程。例如：

```java
class DemoTest {
    @MockDiagnose(LogLevel.ENABLE)
    public static class Mock {
        ...
    }
}
```

输出日志示例如下：

```text
[DIAGNOSE] Handling test class com/alibaba/testable/demo/basic/DemoMockTest
[DIAGNOSE]   Found 6 test cases
[DIAGNOSE] Handling mock class com/alibaba/testable/demo/basic/DemoMockTest$Mock
[DIAGNOSE]   Found 8 mock methods
[DIAGNOSE] Handling source class com/alibaba/testable/demo/basic/DemoMock
[DIAGNOSE]   Handling method <init>
[DIAGNOSE]   Handling method newFunc
[DIAGNOSE]     Line 19, mock method "createBlackBox" used
[DIAGNOSE]   Handling method outerFunc
[DIAGNOSE]     Line 27, mock method "innerFunc" used
[DIAGNOSE]     Line 27, mock method "staticFunc" used
[DIAGNOSE]   Handling method commonFunc
[DIAGNOSE]     Line 34, mock method "trim" used
[DIAGNOSE]     Line 34, mock method "sub" used
[DIAGNOSE]     Line 34, mock method "startsWith" used
... ...
```

其中`Line XX, mock method "XXX" used`日志展示了被测类中所有发生了Mock替换的调用和相应代码行号。

简单排查方法：

- 若没有任何输出，请检查`pom.xml`或`build.gradle`配置是否正确引入了`TestableMock`依赖
- 若只输出了`Handling mock class`，请检查Mock容器类的名称和位置是否符合规范
- 若只输出了`Handling mock class`和`Handling test class`，请检查被测类与测试类是否包路径相同，且名称为"被测类+Test"，或者是否正确的使用了`@MockWith`注解
- 若输出了`Handling source class`以及`Handling method xxx`，但预期的代码行位置没有发生Mock替换，请继续检查Mock方法定义是否未与目标方法匹配

对于上述的最后一种情况（预期Mock未生效），可将日志级别提升到`LogLevel.VERBOSE`做进一步排查。例如：

```java
class DemoTest {
    @MockDiagnose(LogLevel.VERBOSE)
    public static class Mock {
        ...
    }
}
```

再次执行单元测试，此时将会打印出所有Mock方法的签名定义，以及被测类中扫描到所有调用的实际方法签名：

```text
[DIAGNOSE] Handling test class com/alibaba/testable/demo/basic/DemoMockTest
[VERBOSE]    Test case "should_mock_new_object"
... ...
[VERBOSE]    Test case "should_set_mock_context"
[DIAGNOSE]   Found 6 test cases
[DIAGNOSE] Handling mock class com/alibaba/testable/demo/basic/DemoMockTest$Mock
[VERBOSE]    Mock constructor "createBlackBox" as "com.alibaba.demo.basic.model.mock.BlackBox(java.lang.String)"
[VERBOSE]    Mock method "innerFunc" as "com.alibaba.demo.basic.DemoMock::innerFunc(java.lang.String) : java.lang.String"
... ...
[VERBOSE]    Mock method "callFromDifferentMethod" as "()Ljava/lang/String;"
[DIAGNOSE]   Found 8 mock methods
[DIAGNOSE] Handling source class com/alibaba/testable/demo/basic/DemoMock
[DIAGNOSE]   Handling method <init>
[VERBOSE]      Line 13, constructing "java.lang.Object()"
[DIAGNOSE]   Handling method newFunc
[VERBOSE]      Line 19, constructing "com.alibaba.demo.basic.model.mock.BlackBox(java.lang.String)"
[DIAGNOSE]     Line 19, mock method "createBlackBox" used
[VERBOSE]      Line 19, invoking "com.alibaba.demo.basic.DemoMockTest$Mock::createBlackBox(java.lang.String) : com.alibaba.demo.basic.model.mock.BlackBox"
[VERBOSE]      Line 20, invoking "com.alibaba.demo.basic.model.mock.BlackBox::get() : java.lang.String"
[DIAGNOSE]   Handling method outerFunc
[VERBOSE]      Line 27, constructing "java.lang.StringBuilder()"
[VERBOSE]      Line 27, invoking "java.lang.StringBuilder::append(java.lang.String) : java.lang.StringBuilder"
[VERBOSE]      Line 27, invoking "com.alibaba.demo.basic.DemoMock::innerFunc(java.lang.String) : java.lang.String"
[DIAGNOSE]     Line 27, mock method "innerFunc" used
... ...
```

输出日志结构参考如下：

- `Mock constructor "<Mock方法名>" as "<方法签名>" for "<类型>"` 在测试类中扫描到的**Mock构造方法**及其签名
- `Mock method "<Mock方法名>" as "<方法签名>"` 在测试类中扫描到的**普通Mock方法**及其签名
- `Line XX, constructing "<类型>" as "<方法签名>"` 在被测类中扫描掉的**构造方法调用**及其签名
- `Line XX, invoking "<方法名>" as "<方法签名>"` 在被测类中扫描到的**成员方法调用**及其签名
