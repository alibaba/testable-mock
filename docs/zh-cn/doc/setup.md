使用TestableMock
---

`TestableMock`现在已不仅是一款轻量易上手的单元测试Mock工具，更是以**简化Java单元测试**为目标的综合辅助工具集，包含以下功能：

- [快速Mock任意调用](zh-cn/doc/use-mock.md)：使被测类的任意方法调用快速替换为Mock方法，实现"指哪换哪"，解决传统Mock工具使用繁琐的问题
- [访问被测类私有成员](zh-cn/doc/private-accessor.md)：使单元测试能直接调用和访问被测类的私有成员，解决私有成员初始化和私有方法测试的问题
- [快速构造参数对象](zh-cn/doc/omni-constructor.md)：生成任意复杂嵌套的对象实例，并简化其内部成员赋值方式，解决被测方法参数初始化代码冗长的问题
- [辅助测试void方法](zh-cn/doc/test-void-method.md)：利用Mock校验器对方法的内部逻辑进行检查，解决无返回值方法难以实施单元测试的问题
- [快速测试数据库SQL](zh-cn/doc/verify-sql.md)：通过内置常见数据库访问包的Mock实现，解决数据访问层(DAO层)代码逻辑难以直接测试验证的问题

## 在Maven项目中使用

在项目`pom.xml`文件中，增加`testable-all`依赖和`maven-surefire-plugin`配置，具体方法如下。

建议先添加一个标识TestableMock版本的`property`，便于统一管理：

```xml
<properties>
    <testable.version>0.6.6</testable.version>
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
    testImplementation('com.alibaba.testable:testable-all:0.6.6')
    testAnnotationProcessor('com.alibaba.testable:testable-processor:0.6.6')
}
```

然后在测试配置中添加javaagent：

```groovy
test {
    jvmArgs "-javaagent:${classpath.find { it.name.contains("testable-agent") }.absolutePath}"
}
```

参见项目`java-demo`的[build.gradle](https://github.com/alibaba/testable-mock/blob/master/demo/java-demo/build.gradle)和`kotlin-demo`的[build.gradle.kts](https://github.com/alibaba/testable-mock/blob/master/demo/kotlin-demo/build.gradle.kts)文件。

> 若用于Android项目，则添加`TestableMock`依赖方法同上，添加javaagent配置方法如下：
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
> 完整代码可参考`demo/android-demo`示例项目。

> 若项目使用`Spock`测试框架，需指定`Groovy`编译生成的JVM **1.6或以上**版本字节码，方法如下（请根据实际使用的JVM版本修改属性值）。
> 
> Maven项目在`pom.xml`中添加`<maven.compiler.source>`和`<maven.compiler.target>`属性，例如：
> ```xml
> <properties>
>   <!-- 或 1.7/1.8/... -->
>   <maven.compiler.source>1.6</maven.compiler.source>
>   <maven.compiler.target>1.6</maven.compiler.target>
> </properties>
> ```
> 
> Gradle项目在`build.gradle`中添加`sourceCompatibility`属性，例如：
> ```groovy
> sourceCompatibility = '6'  // 或7/8/9/...
> ```
> 
> 完整代码可参考`demo/spock-demo`示例项目。
