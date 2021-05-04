使用包路径映射
---

虽然在规范的单元测试中，测试编写者应当关注于被测类自身的业务逻辑，将无关外部调用按需替换为Mock，然而现实中的单元测试通常总是会调用到一部分外部类的逻辑。

`TestableMock`基于"让每个业务类提供自己的Mock方法集合"的设计原则，默认约定包含Mock方法集合的容器类型应与业务类具有相同的包路径，否则需显式的[使用MockWith注解](zh-cn/doc/use-mock-with.md)。当遇到需Mock的逻辑属于某个三方包时，由于无法直接修改代码添加`@MockWith`注解，为了符合包路径约定，会导致在测试代码里出现孤立的"飞包"现象：

```
src
├── main
│   └── java
│       └── com
│           └── demo
│               └── biz
│                   ├── service
│                   │   ├── AaService.java
│                   │   ├── BbService.java
│                   │   ...
│                   ├── util
│                   │   └── ToolUtil.java
│                   ...
└── test
    └── java
        └── com
            ├── demo
            │   └── biz
            │       ├── service
            │       │   ├── AaServiceTest.java
            │       │   ├── BbServiceTest.java
            │       │   ...
            │       ├── util
            │       │   └── ToolUtilTest.java
            │       ...
            └── 3rd
                └── party
                    └── pkg
                        └── SomethingMock.java  <- 孤立的"飞包"
```

包路径映射功能就是用来解决这个问题的。在[`testable.properties`配置文件](zh-cn/doc/javaagent-args.md)中，添加以`mock.package.mapping`开头的配置项，具体格式为：

```properties
mock.package.mapping.<业务类所在包路径> = <Mock类所在包路径>
```

例如：

```properties
mock.package.mapping.com.3rd.party.pkg = com.demo.biz.3rd
```

此时若需要Mock的代码位于`com.3rd.party.pkg`下的`Something`类型里，则只需在测试目录的`com.demo.biz.3rd`包中创建`SomethingMock`类型，然后在其中添加需Mock的目标方法即可。

使用映射后的测试包目录结构如下，"飞包"问题不复存在了。

```
src
├── main
│   └── ...
└── test
    └── java
        └── com
            ├── demo
            │   └── biz
            │       ├── 3rd
            │       │   └── SomethingMock.java  <- 三方包Mock容器类
            │       ├── service
            │       │   ├── AaServiceTest.java
            │       │   ├── BbServiceTest.java
            │       │   ...
            │       ├── util
            │       │   └── ToolUtilTest.java
            │       ...
```

可根据实际情况，在配置文件中添加任意多条不同的路径映射。

> 注意：上述示例中的`3rd`是非法的Java包路径名称，仅用于功能介绍目的

对于包映射功能的几项提醒：

- 对三方包使用范围为`ASSOCIATED`的Mock方法没有任何意义（该方法将永远不会生效），请使用`scope`为`GLOBAL`的Mock方法
- 虽然映射后的路径同样支持用测试类中的`Mock`内部类作为Mock容器，但通常更建议直接使用`<业务类>+Mock`方式命名的独立Mock容器类，以便于代码阅读
- 在多模块的Maven或Gradle工程中，由于模块之间是Jar包集成，若需要进行跨模块Mock，应在测试用例所在模块里提供单独的Mock容器类，并根据情况决定是否需要进行包路径映射
