已知问题
---

#### 1. 访问私有方法或私有成员代码在IDE提示语法错误

使用`@EnablePrivateAccessor`注解后访问私有方法或成员变量，虽然能正常通过编译，但在IDE上依然会提示语法错误。
这个问题与使用`Lombok`工具库后使用生成的`getter`和`setter`会被IDE报语法错误一样，需要通过IDE插件来解决。
当前`TestableMock`尚未提供相关插件。也可以改用`PrivateAccessor`工具类来访问私有成员，来避免IDE的异常信息。

#### 2. 通过IDE运行单个测试用例时，Mock功能失效

这是由于IDE运行单个测试用例时只会运行`maven-surefire-plugin`插件，跳过了`testable-maven-plugin`插件执行，导致Mock功能所需的JavaAgent没有随测试启动。

解决方法有两种：

**方法一**：在单元测试配置的"虚拟机参数（VM Option）"属性值末尾添加JavaAgent启动参数：`-javaagent:${HOME}/.m2/repository/com/alibaba/testable/testable-agent/0.3.0/testable-agent-0.3.0.jar`

> PS：请将路径中的版本号替换成实际使用的版本号

![idea-vm-option](https://testable-code.oss-cn-beijing.aliyuncs.com/idea-vm-option.png)

**方法二**：不使用`testable-maven-plugin`插件，直接配置JavaAgent参数到`maven-surefire-plugin`插件上。（`JMockit`也是使用了这种方法）配置方法为：

> PS：请将路径中的版本号替换成实际使用的版本号

```xml
<plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <configuration>
                <argLine>-javaagent:${settings.localRepository}/com/alibaba/testable/testable-agent/0.2.2/testable-agent-0.2.2.jar</argLine>
        </configuration>
</plugin>
```

用这种方法需要注意，如果项目同时还使用了`Jacoco`的`on-the-fly`模式（默认模式）统计单元测试覆盖率，则需要在参数中再添加一个`@{argLine}`参数，完整配置如下：

```xml
<plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <configuration>
                <argLine>@{argLine} -javaagent:${settings.localRepository}/com/alibaba/testable/testable-agent/0.2.2/testable-agent-0.2.2.jar</argLine>
        </configuration>
</plugin>
```
