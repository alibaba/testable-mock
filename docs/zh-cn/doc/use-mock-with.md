使用MockWith注解
---

`@MockWith`注释的功能是为测试显式指定Mock容器，通常用于测试类或Mock容器类没有在标准约定位置的情况，以下列举几种典型使用场景。

### 1. 非标准位置的Mock容器类

`TestableMock`会依次在以下两个位置寻找Mock容器：

- 默认位置测试类中名为`Mock`的静态内部类（譬如原类型是`Demo`，Mock容器类为`DemoTest.Mock`）
- 同包路径下名为`被测类+Mock`的独立类（譬如原类型是`Demo`，Mock容器类为`DemoMock`）

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

此时需要在测试类上显式的指定相应的Mock容器类，不过这种情况在实际中并不太常见。

### 2. 非标准位置的测试类

从`TestableMock`的原理来说，测试类的位置其实只是作为“被测类”与“Mock容器类”之间建立关联的参照物。当测试类的位置不是默认约定的`被测类+Test`时，上述首选的Mock容器位置就不成立了。但此时次选Mock位置依然可用，即如果Mock容器类的位置是`被测类+Mock`，那么Mock置换就依然能够正常进行。

但此时测试类与Mock容器之间的关联丢失了，因此需要为测试类使用`@MockWith`注解来显式的建立关联。例如：

```java
public class DemoService {       // 被测类
    ...
}

public class DemoServiceMock {   // Mock容器类
    ...
}

@MockWith(DemoServiceMock.class) // 测试类由于丢失与Mock容器的关联，需要@MockWith注解
public class ServiceTest {
    ...
}
```

另一种相对少见的情况是Mock容器采用测试类的静态内部类，但测试类由于某些原因无法置于约定位置（或无法遵循约定命名）。此时测试类与Mock类的关联能够自动建立，但被测类无法找到自己的Mock容器，因此需要在**被测类**上添加`@MockWith`注解来显式的建立关联。例如：

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

对于更特殊的一种情况，即测试类与Mock容器类均不在约定位置的时候，则需要同时在**测试类**与**被测类**上都使用`@MockWith`指向同一个Mock容器类来建立三者的关联。复杂的关联对代码阅读会造成一定不便，在实际运用中应当尽量避免这种情况发生。

> 特别说明：`@MockWith`默认使用被注解类名字的尾缀判断当前类是被测类（名字非`Test`结尾）还是测试类（名字`Test`结尾），若遇到不符合此规则的类型，应使用注解的`treatAs`参数显式的指定（`ClassType.SourceClass`-被测类/`ClassType.TestClass`-测试类）

> Q：为何当测试类在非约定位置时，是在被测类上使用`@MockWith`，而不在测试类上指定被测类？
>
> A：从原则上来说，凡是能只改测试类就实现的，肯定不应该为了测试而去动业务代码（被测类）。
> 然而由于JavaAgent只能在类首次加载进内存的时候进行字节码处理，实际情况无法保证被测的类一定在测试类之后加载（可能在其他测试用例执行的时候就被提前加载进内存了），等读取到测试类上的信息时，可能已经无法对被测类进行Mock处理。因此对于测试类和被测类相互不知道对方位置的情况，采用了两边都用`@MockWith`指定Mock容器类的折中设计。

### 3. 在一个测试类中测试多个被测类

这是非标准位置测试类的一种特殊情况，当一个测试类里同时测试了多个业务类（被测类），其名称要么只能与其中某个被测类有`+Test`的命名符合，要么不与其中任何一个被测类有命名相关性。

假设所有被测类的Mock容器均采用`被测类+Mock`约定命名（否则参考前一条规则，被测类也需要显式加`@MockWith`）。若该测试类本身命名不符合其中任何一个被测类+Test约定的情况，需要为该测试类加一个无参数的`@MockWith`注解（即使用默认值，相当于`@MockWith(NullType.class)`），用于标识此类需参与`TestableMock`的预处理。

完整代码示例见`java-demo`和`kotlin-demo`示例项目中`OneToMultiSvcTest`测试类的用例。

> 由于测试类无法通过`@MockWith`与多个Mock容器关联，目前这种用法仅支持生效范围为`MockScope.GLOBAL`的Mock方法。

### 4. 使用不包含Mock方法的Mock容器类

为了加快搜索Mock容器类的速度，在扫描过程中，`TestableMock`只会将自身定义有Mock方法（包含`@MockMethod`或`@MockMockConstructor`注解的方法）以及明确被`@MockWith`指向的类识别为有效的Mock容器，而不会去遍历其父类。

假如出于某些极特殊原因要使用无Mock方法的类型作为Mock容器，譬如希望将实际Mock方法均定义在父类，实际使用的子容器仅仅重载父类的某些特定方法。此时即使Mock容器类的位置符合约定，为了能够被识别，依然应该在相应的测试类上增加对Mock容器类的`@MockWith`引用。
