复用Mock类与方法
---

“Don't Repeat Yourself”是软件开发过程当中的一项重要原则（即“DRY原则”），在编写测试代码时，有些通用的基础功能调用语句常常出现在许多相似的业务类里，若测试中需要Mock这些调用，就要在各个测试类中重复提供同样的Mock方法。而通过Mock方法的复用机制，能够很好的避免编写臃肿重复Mock代码的麻烦。

TestableMock支持两种粒度的Mock复用方式：<u>复用Mock类</u>和<u>复用Mock方法</u>。

## 复用Mock类

如果有两个或以上测试类需要Mock的方法近乎相同，那么采用类级别的Mock复用就是最省心的一种方式。

进行类级别的Mock复用，只需将Mock容器定义为独立的类，然后在要使用它的测试类上通过`@MockWith`进行引用。例如：

```java
@MockWith(ServiceMock.class)
public class AaaServiceTest {
    ...
}

@MockWith(ServiceMock.class)
public class BbbServiceTest {
    ...
}

public class ServiceMock {
    ...
}
```

这样在`AaaServiceTest`和`BbbServiceTest`类中的测试用例在执行时，都会用`ServiceMock`容器类中定义的Mock方法进行调用匹配和Mock替换。

## 复用Mock方法

实际场景中，相比一次性复用整个Mock类的情况，更常见的是对部分高频Mock方法进行复用。

Mock方法的复用可以通过Mock容器类的继承来实现，父类中定义的所有Mock方法都会在子类中自然存在，例如：

```java
public class AaaServiceTest {
    public static class Mock extends BasicMock {
        ...
    }
    ...
}

public class BbbServiceTest {
    public static class Mock extends BasicMock {
        ...
    }
    ...
}

public class BasicMock {
    @MockMethod(targetClass = UserDao.class)
    protected String getById(int id) {
        ...
    }
}
```

则名为`getById`的Mock方法在`AaaServiceTest`和`BbbServiceTest`的测试用例执行时都会生效。

