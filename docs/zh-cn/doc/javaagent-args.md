全局运行参数
---

`TestableMock`的许多功能采用了基于JavaAgent的运行时字节码修改实现。在JavaAgent启动时，可以通过全局配置文件调整其执行过程。

## 1.使用全局配置文件

在项目的`src/test/resources`目录下创建一个名为`testable.properties`的文本文件，其中每行是一条配置项，配置项的名称与值之间用`=`连接。

详细的可用配置项列说明下：

|  配置项  | 描述  | 可用值和示例 |
|  ----   | ---- | ----  |
| dump.path  | 将修改过后的字节码保存到本地指定目录（用于排查问题） | 相对项目根目录的位置，例如：`target/bytecode` |
| enhance.pkgPrefix.includes  | 让`TestableMock`仅在特定包内生效，通常用于大型项目 | 使用`,`分隔的包路径前缀列表，例如：`com.demo.biz,com.3rd.biz` |
| log.file  | 指定TestableAgent日志文件位置 | 相对项目根目录的位置，例如：`target/testable/agent.log`，特殊值`null`表示禁用日志文件 |
| log.level  | 修改全局日志级别 | 可用值为：`mute`（禁止打印警告） / `debug`（打印调试信息） / `verbose`（打印非常详细的调试信息） |
| mock.innerClass.name | 修改测试类中的内置Mock容器类名 | 任意符合Java类名的值，默认值为`Mock` |
| mock.scope.default  | 修改默认的Mock生效范围（详见[Mock生效范围](zh-cn/doc/scope-of-mock)） | 可用值为：`global`（全局生效） / `associated`（只对关联的测试用例生效） |
| mock.target.checking.enable | 是否启用Mock目标有效性的前置检查 | 可用值为：`true` / `false`，当前默认值为`false` |
| omni.constructor.enhance.enable | 是否启用`OmniConstructor`的字节码增强模式 | 可用值为：`true` / `false` |
| omni.constructor.enhance.pkgPrefix.excludes | 对特定包禁用`OmniConstructor`的字节码增强模式 | 使用`,`分隔的包路径前缀列表，例如：`com.demo.model` |
| thread.pool.enhance.enable | 是否启用基于`TransmittableThreadLocal`的Mock上下文存储 | 可用值为：`true` / `false` |

参见`demo`目录各示例项目中的`testable.properties`文件。

## 2.修改配置文件位置

除了配置文件，`TestableMock`也支持通过引入`testable-agent`包时，在末尾加上一个`=`符号，然后连接额外参数来实现全局配置。这种方法与使用配置文件基本等效，因此通常无需使用，但有唯一特例是`configFile`配置项，该配置项可用于修改全局配置文件的位置。

|  参数          | 描述  | 可用值和示例 |
|  ----         | ----  | ----  |
| configFile    | 修改全局配置文件位置 | 可用相对路径或绝对路径，当为相对路径时，表示相对项目根目录的位置，默认值为`src/test/resources/testable.properties` |

对于Maven项目，可将参数追加到`maven-surefire-plugin`参数`testable-agent`包尾部，紧接着`.jar`的位置。例如将配置文件位置修改为项目根路径下的`config/testable.properties`文件：

```xml
<configuration>
    <argLine>-javaagent:${settings.localRepository}/com/alibaba/testable/testable-agent/${testable.version}/testable-agent-${testable.version}.jar=configFile=config/testable.properties</argLine>
</configuration>
```

对于Gradle项目，同样是直接将参数追加到引入`testable-agent`的配置末尾。例如：

```groovy
    jvmArgs "-javaagent:${classpath.find { it.name.contains("testable-agent") }.absolutePath}=configFile=config/testable.properties"
```

## 3. 全局配置技巧

对于一般的小型项目，`TestableMock`的所有功能都是开箱即用的，正确引用依赖包后，无需进行额外配置。

默认情况下，`TestableMock`会在构建目录中（Maven构建的`target`目录或Gradle构建的`build`目录）自动生成一个记录执行过程的`testable-agent.log`文件。若希望禁用此日志文件，可将`log.file`参数赋值为`null`。

若测试中使用了`OmniConstructor`且遇到构造出错的情况，可开启`omni.constructor.enhance.enable`配置（倘若开启后依然报错，请提Issue告诉我们）。`omni.constructor.enhance.pkgPrefix.excludes`配置主要用于当开启`OmniConstructor`字节码增强模式报错时，临时绕过某些无法处理的类型，通常无需使用。

若项目较大（构建生成的jar包大于100MB），可考虑使用`enhance.pkgPrefix.includes`参数来减少`TestableMock`在测试启动前建立Mock关联和进行`OmniConstructor`预处理的扫描时长，从而加快单元测试启动速度。通常将值设置为当前项目自身的`<group>.<artifact>`路径即可，如需Mock三方包中的调用，或通过`OmniConstructor`构造来自三方包中的类型，则还应该加上相应的三方包路径。

若项目测试中，既包含真实的单元测试，又包含了使用单元测试框架编写的集成测试时。为了让集成测试的执行过程不受Mock影响，可能需要使用`mock.scope.default`将默认的Mock方法范围限制为仅对所属类型的单元测试用例生效。

若需Mock的调用发生在线程池中，且遇到`verify()`结果或`MOCK_CONTEXT`内容不正确的时候，则需考虑开启`thread.pool.enhance.enable`配置，详见[Mock线程池内的调用](zh-cn/doc/with-thread-pool.md)。
