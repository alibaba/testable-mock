全局运行参数
---

`TestableMock`的许多功能采用了基于JavaAgent的运行时字节码修改实现。在JavaAgent启动时，可以通过两种方式调整`TestableMock`的执行过程。

## 1.使用全局配置文件

在项目的`src/test/resources`目录下创建一个名为`testable.properties`的文本文件，其中每行为一条配置项，配置项的名称与值之间用`=`连接。

对于大多数情况，我们更推荐使用配置文件来调节`TestableMock`的行为，这种方式相比Agent参数更加易读。

### 可用配置项

|  配置项  | 描述  | 可用值和示例（`N/A`表示无需赋值） |
|  ----   | ---- | ----  |
| dump.path  | 将修改过后的字节码保存到本地指定目录（用于排查问题） | 相对项目根目录的位置，例如：`target/bytecode` |
| enhance.pkgPrefix.includes  | 让`TestableMock`仅在特定包内生效，通常用于大型项目 | 使用`,`分隔的包路径前缀列表，例如：`com.demo.biz,com.3rd.biz` |
| log.file  | 指定TestableAgent日志文件位置 | 相对项目根目录的位置，例如：`target/testable/agent.log`，特殊值`null`表示禁用日志文件 |
| log.level  | 修改全局日志级别 | 可用值为：`mute`（禁止打印警告） / `debug`（打印调试信息） / `verbose`（打印非常详细的调试信息） |
| mock.innerClass.name | 修改测试类中的内置Mock容器类名 | 任意符合Java类名的值，默认值为`Mock` |
| mock.scope.default  | 修改默认的Mock生效范围（详见[Mock生效范围](zh-cn/doc/scope-of-mock)） | 可用值为：`global`（全局生效） / `associated`（只对关联的测试用例生效） |
| omni.constructor.enhance.enable | 是否启用`OmniConstructor`的字节码增强模式 | 可用值为：`true` / `false` |
| omni.constructor.enhance.pkgPrefix.excludes | 对特定包禁用`OmniConstructor`的字节码增强模式 | 使用`,`分隔的包路径前缀列表，例如：`com.demo.model` |
| thread.pool.enhance.enable | 是否启用基于`TransmittableThreadLocal`的Mock上下文存储 | 可用值为：`true` / `false` |

示例见`java-demo`和`kotlin-demo`项目中的`testable.properties`文件。

## 2.使用全局Agent参数

与其它基于JavaAgent的工具相似，`TestableMock`也支持通过在引入`testable-agent`包时，在末尾加上一个`=`符号，然后连接额外参数来传递用户的自定义参数。

### 可用参数清单

|  参数          | 描述  | 可用值和示例（`N/A`表示无需赋值） |
|  ----         | ----  | ----  |
| configFile    | 修改全局配置文件位置 | 相对项目根目录的位置，默认值为`src/test/resources/testable.properties` |
| logLevel      | 修改全局日志级别 | 可用值为：`mute`（禁止打印警告） / `debug`（打印调试信息） / `verbose`（打印非常详细的调试信息） |
| logFile       | 指定TestableAgent日志文件位置 | 相对项目根目录的位置，例如：`target/testable/agent.log`，特殊值`null`表示禁用日志文件 |
| dumpPath      | 将修改过后的字节码保存到本地指定目录（用于排查问题） | 相对项目根目录的位置，例如：`target/bytecode` |
| pkgPrefix     | 让`TestableMock`仅在特定包内生效，通常用于大型项目 | 使用`,`分隔的包路径前缀列表，例如：`com.demo.biz,com.3rd.biz` |
| mockScope     | 修改默认的Mock生效范围（详见[Mock生效范围](zh-cn/doc/scope-of-mock)） | 可用值为：`global`（全局生效） / `associated`（只对关联的测试用例生效） |
| useThreadPool | 启用基于`TransmittableThreadLocal`的Mock上下文存储（用于包含线程池的测试用例） | `N/A` |

### 参数的连接

若参数有值，参数名和值之间用`=`符合连接。例如：

`useThreadPool`、`logLevel=debug`、`dumpPath=/tmp/debug`

多个参数之间使用`&`符号连接，例如：

`useThreadPool&logLevel=debug`、`logLevel=debug&dumpPath=/tmp/debug`

### 添加运行参数

对于Maven项目，可将参数追加到`maven-surefire-plugin`参数`testable-agent`包尾部，紧接着`.jar`的位置。例如：

```xml
<configuration>
    <argLine>-javaagent:${settings.localRepository}/com/alibaba/testable/testable-agent/${testable.version}/testable-agent-${testable.version}.jar=mockScope=associated&amp;pkgPrefix=com.demo</argLine>
</configuration>
```

> 注意：在`xml`文件中，连接参数的`&`符号需要写为`&amp;`

对于Gradle项目，同样是直接将参数追加到引入`testable-agent`的配置末尾。例如：

```groovy
    jvmArgs "-javaagent:${classpath.find { it.name.contains("testable-agent") }.absolutePath}=mockScope=associated&pkgPrefix=com.demo"
```
