使用Testable Maven插件
---

在使用Maven构建的项目里，除了直接修改`maven-surefire-plugin`插件的运行参数，也可通过`testable-maven-plugin`插件获得相同效果：

```xml
<plugin>
    <groupId>com.alibaba.testable</groupId>
    <artifactId>testable-maven-plugin</artifactId>
    <version>${testable.version}</version>
    <executions>
        <execution>
            <id>prepare</id>
            <goals>
                <goal>prepare</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

> 当使用`testable-maven-plugin`插件时，应该移除`maven-surefire-plugin`插件上的TestableMock相关配置，同时可以从项目的`pom.xml`文件中移除对`com.alibaba.testable:testable-agent`包的显示依赖

`testable-maven-plugin`插件能够与Jacoco插件直接同时使用，无需额外适配，因此能使`pom.xml`文件编写起来更简单且美观。

但是，当通过IDE运行单个测试用例时，Mock功能会失效。

这是由于IDE运行单个测试用例时通常都只会运行`maven-surefire-plugin`插件，跳过了`testable-maven-plugin`插件执行，导致Mock功能所需的JavaAgent没有随测试注入。

该问题可以通过额外配置IDE的测试参数绕过。以IntelliJ为例，在单元测试配置的"虚拟机参数（VM Option）"属性值末尾添加JavaAgent启动参数：`-javaagent:${HOME}/.m2/repository/com/alibaba/testable/testable-agent/x.y.z/testable-agent-x.y.z.jar`

> PS：请将路径中的`x.y.z`替换成实际使用的版本号

![idea-vm-option](https://testable-code.oss-cn-beijing.aliyuncs.com/idea-vm-option.png)

这样实际上还是该了`maven-surefire-plugin`插件的配置，因此目前除了需要考虑美观因素的场景以外，直接在`pom.xml`文件中修改`maven-surefire-plugin`插件配置依然是更加实用的方案。
