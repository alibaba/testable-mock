使用TestableMock
---

`TestableMock`是基于源码和字节码增强的Java单元测试辅助工具，包含以下功能：

- [访问被测类私有成员](zh-cn/doc/private-accessor.md)：使单元测试能直接调用和访问被测类的私有成员，解决私有成员初始化和私有方法测试的问题
- [快速Mock任意方法](zh-cn/doc/use-mock.md)：使被测类的任意方法调用快速替换为Mock方法，实现"指哪换哪"，解决传统Mock工具使用繁琐的问题

## 在Maven项目中使用

在`pom.xml`文件中添加`testable-processor`依赖：

```xml
<dependency>
    <groupId>com.alibaba.testable</groupId>
    <artifactId>testable-processor</artifactId>
    <version>${testable.version}</version>
    <scope>provided</scope>
</dependency>
```

以及`testable-maven-plugin`插件：

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

> 其中`${testable.version}`需替换为具体版本号，当前最新版本为`0.3.0`

若仅需使用单元测试随意访问被测类私有字段和方法的能力，不使用Mock功能，则`testable-maven-plugin`插件可以省略。

## 在Gradle项目中使用

在`build.gradle`文件中添加`testable-processor`依赖：

```groovy
dependencies {
    testCompile('com.alibaba.testable:testable-processor:0.3.0')
}
```

然后在测试配置中添加javaagent：

```groovy
test {
    jvmArgs "-javaagent:${classpath.find { it.name.contains("testable-agent") }.absolutePath}"
}
```

> 该配置尚未在Gradle项目上经过实际验证，可行性待确认。
