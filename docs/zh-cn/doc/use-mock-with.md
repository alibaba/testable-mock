使用MockWith注解
---

`@MockWith`注释的功能是为测试显式指定Mock容器，通常用于测试类或Mock容器类没有在标准约定位置的情况，以下列举几种典型使用场景。

### 1. 非标准位置的Mock容器类

`TestableMock`会依次在以下两个位置寻找Mock容器：

- 测试类中名为`Mock`的静态内部类（譬如原类型是`Demo`，测试类是`DemoTest`，Mock容器类为`DemoTest.Mock`）
- 同包路径下寻找名为`被测类+Mock`的外部类（譬如原类型是`Demo`，测试类是`DemoTest`，Mock容器类为`DemoMock`）
  
倘若实际要使用的Mock容器类不在这两个位置，就需要在测试类上使用`@MockWith`注释了。一般来说，造成Mock容器类不在默认位置的原因可能有两种：复用Mock容器、集中管理Mock容器。

当对一批功能近似的类型进行测试的时候，由于需要进行Mock的外部调用基本一致，可以将这些类型所需的所有Mock方法集中写在一个Mock容器类里，然后让相关测试类共同引用这个公共Mock容器。详见[复用Mock类与方法](zh-cn/doc/mock-method-reusing.md)文档。

另一种情况是开发者希望将Mock方法的定义与测试类本身分开，以便进行集中管理（或规避某些扫描工具的路径规则），譬如形成下面这种目录结构：

```
src/
  main/
    com/
      demo/
        service/
          DemoService.java
  test/
    com/
      demo/
        service/
          DemoServiceTest.java
      mock/
        service/
          DemoServiceMock.java
```

此时就需要在测试类上显式的指定相应的Mock容器类了，这种场景在实际情况中比较少见。

### 2. 非标准位置的测试类

`TestableMock`在建立“被测类”、“测试类”、“Mock容器类”之间关联的过程中，为了识别被测类的位置，约定测试类应当与被测类在相同的包路径，且名称为`被测类+Test`。当实际情况不符合这种约定的时候，就需要通过在**被测类**上添加`@MockWith`注解来显式的建立关联。例如：

```java
@MockWith(ServiceTest.Mock.class)
public class DemoService {
    ...
}

public class ServiceTest {
    public static class Mock extends BasicMock {
        ...
    }
}
```

> 注意：
> 1. `@MockWith`注解通常是使用在测试类上，但这种情况下需要用在被测类上
> 2. `@MockWith`指向的目标始终是Mock容器类，而不是测试类
> 3. `@MockWith`默认使用被注解类名字的尾缀判断当前类是被测类（名字非`Test`结尾）还是测试类（名字`Test`结尾），若遇到不符合此规则的类型，应使用注解的`treatAs`参数显式的指定（`ClassType.SourceClass`-被测类/`ClassType.TestClass`-测试类）

更进一步来说，若出现测试类与Mock容器类均不在约定位置的时候，则需要同时在**测试类**与**被测类**上都使用`@MockWith`指向同一个Mock容器类来建立三者的关联，但这种场景在实际运用中很少会遇到。

### 3. 使用不包含Mock方法的Mock容器类

为了加快搜索Mock容器类的速度，在扫描过程中，`TestableMock`只会将自身定义有Mock方法（包含`@MockMethod`或`@MockMockConstructor`注解的方法）以及明确被`@MockWith`指向的类识别为有效的Mock容器，而不会去遍历其父类。

对于某些特殊情况，譬如希望将实际Mock方法均定义在父类，实际使用的子容器仅仅重载父类的某些特定方法，此时即使Mock容器类的位置符合约定，为了能够被识别，依然应该在相应的测试类上增加对Mock容器类的`@MockWith`引用。
