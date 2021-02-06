自助问题排查
---

相比`Mockito`等由开发者手工放置Mock类的做法，`TestableMock`使用方法名和参数类型匹配自动寻找需Mock的调用。这种机制在带来方便的同时也有可能发生预料之外的Mock替换。

若要排查Mock相关的问题，只需在测试类上添加`@MockWith`注解，并配置参数`diagnose`值为`MockDiagnose.ENABLE`，在运行测试时就会打印出详细的Mock方法替换过程。

```java
@MockWith(diagnose = MockDiagnose.ENABLE)
class DemoTest {
    ...
}
```

输出日志示例如下：

```text
[DIAGNOSE] Handling test class com/alibaba/testable/demo/DemoMockTest
[DIAGNOSE] Handling source class com/alibaba/testable/demo/DemoMock
[DIAGNOSE]   Found 7 mock methods
[DIAGNOSE]   Handling method <init>
[DIAGNOSE]   Handling method newFunc
[DIAGNOSE]     Line 14, mock method createBlackBox used
[DIAGNOSE]   Handling method outerFunc
[DIAGNOSE]     Line 22, mock method innerFunc used
[DIAGNOSE]   Handling method commonFunc
[DIAGNOSE]     Line 29, mock method trim used
[DIAGNOSE]     Line 29, mock method sub used
[DIAGNOSE]     Line 29, mock method startsWith used
[DIAGNOSE]   Handling method getBox
[DIAGNOSE]     Line 36, mock method secretBox used
[DIAGNOSE]   Handling method callerOne
[DIAGNOSE]     Line 43, mock method callFromDifferentMethod used
[DIAGNOSE]   Handling method callerTwo
[DIAGNOSE]     Line 47, mock method callFromDifferentMethod used
[DIAGNOSE]   Handling method innerFunc
[DIAGNOSE]   Handling method callFromDifferentMethod
```

该日志展示了被测类中所有发生了Mock替换的调用和相应代码行号。

简单排查方法：

- 若没有任何输出，请检查`pom.xml`或`build.gradle`配置是否正确引入了TestableMock依赖
- 若只输出了第一行`Handling test class`，请检查被测类与测试类是否包路径相同，且名称为"被测类+Test"（`0.4.x`版本要求）
- 若输出了`Handling source class`以及`Handling method xxx`，但预期的代码行位置没有发生Mock替换，请检查Mock方法定义是否未与目标方法匹配
