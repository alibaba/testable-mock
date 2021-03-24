使用TestableMock
---

`TestableMock`是基于源码和字节码增强的Java单元测试辅助工具，包含以下功能：

- [快速Mock任意调用](zh-cn/doc/use-mock.md)：使被测类的任意方法调用快速替换为Mock方法，实现"指哪换哪"，解决传统Mock工具使用繁琐的问题
- [访问被测类私有成员](zh-cn/doc/private-accessor.md)：使单元测试能直接调用和访问被测类的私有成员，解决私有成员初始化和私有方法测试的问题
- [快速构造参数对象](zh-cn/doc/omni-constructor.md)：生成任意复杂嵌套的对象实例，并简化其内部成员赋值方式，解决被测方法参数初始化代码冗长的问题
- [辅助测试void方法](zh-cn/doc/test-void-method.md)：利用Mock校验器对方法的内部逻辑进行检查，解决无返回值方法难以实施单元测试的问题

## 在Maven项目中使用

在项目`pom.xml`文件中，增加`testable-all`依赖和`maven-surefire-plugin`配置，具体方法如下。

建议先添加一个标识TestableMock版本的`property`，便于统一管理：

```xml
<properties>
    <testable.version>0.5.2</testable.version>
</properties>
```

在`dependencies`列表添加TestableMock依赖：

```xml
<dependencies>
    <dependency>
        <groupId>com.alibaba.testable</groupId>
        <artifactId>testable-all</artifactId>
        <version>${testable.version}</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

最后在`build`区域的`plugins`列表里添加`maven-surefire-plugin`插件（如果已包含此插件则只需添加`<argLine>`部分配置）：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <configuration>
                <argLine>-javaagent:${settings.localRepository}/com/alibaba/testable/testable-agent/${testable.version}/testable-agent-${testable.version}.jar</argLine>
            </configuration>
        </plugin>
    </plugins>
</build>
```

若项目同时还使用了`Jacoco`的`on-the-fly`模式（默认模式）统计单元测试覆盖率，则需在`<argLine>`配置中添加一个`@{argLine}`参数，添加后的配置如下：

```xml
<argLine>@{argLine} -javaagent:${settings.localRepository}/com/alibaba/testable/testable-agent/${testable.version}/testable-agent-${testable.version}.jar</argLine>
```

参见项目`java-demo`的[pom.xml](https://github.com/alibaba/testable-mock/blob/master/demo/java-demo/pom.xml)和`kotlin-demo`的[pom.xml](https://github.com/alibaba/testable-mock/blob/master/demo/kotlin-demo/pom.xml)文件。

## 在Gradle项目中使用

在`build.gradle`文件中添加`TestableMock`依赖：

```groovy
dependencies {
    testImplementation('com.alibaba.testable:testable-all:0.5.2')
    testAnnotationProcessor('com.alibaba.testable:testable-processor:0.5.2')
}
```

然后在测试配置中添加javaagent：

```groovy
test {
    jvmArgs "-javaagent:${classpath.find { it.name.contains("testable-agent") }.absolutePath}"
}
```

参见项目`java-demo`的[build.gradle](https://github.com/alibaba/testable-mock/blob/master/demo/java-demo/build.gradle)和`kotlin-demo`的[build.gradle.kts](https://github.com/alibaba/testable-mock/blob/master/demo/kotlin-demo/build.gradle.kts)文件。

> 若是基于`Robolectric`框架的Android项目，则添加`TestableMock`依赖方法同上，添加javaagent配置方法如下：
>
> ```groovy
> android {
>     testOptions {
>         unitTests {
>             all {
>                 jvmArgs "-javaagent:${classpath.find { it.name.contains("testable-agent") }.absolutePath}"
>             }
>         }
>     }
> }
> ```
>
> 完整示例参考[issue-43](https://github.com/alibaba/testable-mock/issues/43)
