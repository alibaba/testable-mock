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
[DIAGNOSE]   Found 8 mock methods
[DIAGNOSE] Handling source class com/alibaba/testable/demo/DemoMock
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

该日志展示了被测类中所有发生了Mock替换的调用和相应代码行号。

简单排查方法：

- 若没有任何输出，请检查`pom.xml`或`build.gradle`配置是否正确引入了TestableMock依赖
- 若只输出了`Handling test class`，请检查被测类与测试类是否包路径相同，且名称为"被测类+Test"（`0.4.x`版本要求）
- 若输出了`Handling source class`以及`Handling method xxx`，但预期的代码行位置没有发生Mock替换，请检查Mock方法定义是否未与目标方法匹配

对于预期Mock未生效的情况，如需进一步排查，可将日志级别提升到`MockDiagnose.VERBOSE`。

```java
@MockWith(diagnose = MockDiagnose.VERBOSE)
class DemoTest {
    ...
}
```

再次执行单元测试，将会打印出所有Mock方法的运行期签名，以及被测类中扫描到所有调用的运行期签名：

```text
[DIAGNOSE] Handling test class com/alibaba/testable/demo/DemoMockTest
[VERBOSE]    Mock constructor "createBlackBox" as "(Ljava/lang/String;)V" for "com/alibaba/testable/demo/model/BlackBox"
[VERBOSE]    Mock method "innerFunc" as "(Ljava/lang/String;)Ljava/lang/String;"
[VERBOSE]    Mock method "staticFunc" as "()Ljava/lang/String;"
[VERBOSE]    Mock method "trim" as "()Ljava/lang/String;"
[VERBOSE]    Mock method "sub" as "(II)Ljava/lang/String;"
[VERBOSE]    Mock method "startsWith" as "(Ljava/lang/String;)Z"
[VERBOSE]    Mock method "secretBox" as "()Lcom/alibaba/testable/demo/model/BlackBox;"
[VERBOSE]    Mock method "callFromDifferentMethod" as "()Ljava/lang/String;"
[DIAGNOSE]   Found 8 mock methods
[DIAGNOSE] Handling source class com/alibaba/testable/demo/DemoMock
[DIAGNOSE]   Handling method <init>
[VERBOSE]      Line 13, constructing "java/lang/Object" as "()V"
[DIAGNOSE]   Handling method newFunc
[VERBOSE]      Line 19, constructing "com/alibaba/testable/demo/model/BlackBox" as "(Ljava/lang/String;)V"
[DIAGNOSE]     Line 19, mock method "createBlackBox" used
[VERBOSE]      Line 19, invoking "createBlackBox" as "(Ljava/lang/String;)Lcom/alibaba/testable/demo/model/BlackBox;"
[VERBOSE]      Line 20, invoking "get" as "()Ljava/lang/String;"
[DIAGNOSE]   Handling method outerFunc
[VERBOSE]      Line 27, constructing "java/lang/StringBuilder" as "()V"
[VERBOSE]      Line 27, invoking "append" as "(Ljava/lang/String;)Ljava/lang/StringBuilder;"
[VERBOSE]      Line 27, invoking "innerFunc" as "(Ljava/lang/String;)Ljava/lang/String;"
[DIAGNOSE]     Line 27, mock method "innerFunc" used
[VERBOSE]      Line 27, invoking "innerFunc" as "(Ljava/lang/String;)Ljava/lang/String;"
[VERBOSE]      Line 27, invoking "append" as "(Ljava/lang/String;)Ljava/lang/StringBuilder;"
[VERBOSE]      Line 27, invoking "staticFunc" as "()Ljava/lang/String;"
[DIAGNOSE]     Line 27, mock method "staticFunc" used
[VERBOSE]      Line 27, invoking "append" as "(Ljava/lang/String;)Ljava/lang/StringBuilder;"
[VERBOSE]      Line 27, invoking "append" as "(Ljava/lang/String;)Ljava/lang/StringBuilder;"
[VERBOSE]      Line 27, invoking "toString" as "()Ljava/lang/String;"
... ...
```

输出日志结构参考如下：

- `Mock constructor "<Mock方法名>" as "<方法签名>" for "<类型>"` 在测试类中扫描到的**Mock构造方法**及其运行时签名
- `Mock method "<Mock方法名>" as "<方法签名>"` 在测试类中扫描到的**普通Mock方法**及其运行时签名（打印时暂未自动排除用于指定Mock目标类的首位参数）
- `Line XX, constructing "<类型>" as "<方法签名>"` 在被测类中扫描掉的**构造方法调用**及其运行时签名
- `Line XX, invoking "<方法名>" as "<方法签名>"` 在被测类中扫描到的**成员方法调用**及其运行时签名
