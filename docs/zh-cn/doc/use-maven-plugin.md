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

**注意：**当使用`testable-maven-plugin`插件时，应该移除`maven-surefire-plugin`插件上的TestableMock相关配置。

`testable-maven-plugin`插件能够与Jacoco插件直接同时使用，无需额外适配，因此能使`pom.xml`文件编写起来更简单且美观。

> 还有一种特殊情况，当`jacoco`插件是通过`maven`命令行参数引入的时候，若要使用`TestableMock`功能，则也必须通过命令行参数引入`testable-maven-plugin`插件。详见[issue-14](https://github.com/alibaba/testable-mock/issues/14)。

但需要注意的是，使用`testable-maven-plugin`插件后，通过IntelliJ IDE运行单个测试用例时，Mock功能会失效。

这是由于IntelliJ IDE运行单个测试用例时只会运行`maven-surefire-plugin`插件，跳过了`testable-maven-plugin`插件执行，导致Mock功能所需的JavaAgent未随测试注入。

该问题可以通过额外配置IDE的测试参数绕过。以IntelliJ为例，打开运行菜单的"编辑配置..."选型，如图中位置①

![modify-run-configuration](https://testable-code.oss-cn-beijing.aliyuncs.com/modify-run-configuration.png)

在"虚拟机参数"属性值末尾添加JavaAgent启动参数：`-javaagent:${HOME}/.m2/repository/com/alibaba/testable/testable-agent/x.y.z/testable-agent-x.y.z.jar`，如图中位置②

> PS：请将路径中的`x.y.z`替换成实际使用的版本号

![add-testable-javaagent](https://testable-code.oss-cn-beijing.aliyuncs.com/add-testable-javaagent.png)

最后点击运行单元测试，如图中位置③

总体来说，由于当下的IDE支持问题，使用`testable-maven-plugin`带来的额外复杂性依然高于其对配置的简化作用。目前直接在`pom.xml`文件中修改`maven-surefire-plugin`插件配置还是相对推荐的实用方案。
