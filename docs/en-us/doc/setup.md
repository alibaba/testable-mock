Use TestableMock
---

`TestableMock` is an assist tool for Java unit testing based on source code and bytecode enhancement, including the following functions:

- [Access private members of the class under test](en-us/doc/private-accessor.md): enable unit tests directly invoke or access private members of the class under test, solve the problems of private member initialization and private method testing
- [Quick mock arbitrary call](en-us/doc/use-mock.md): quickly replace any method invocation in the class under test with a mock method, solve the cumbersome use of traditional mock tools problem
- [Auxiliary test void method](en-us/doc/test-void-method.md): use the mock validator to check the internal logic of method, solve the problem that unit testing is difficult to implement to the method with no return value

## Use in Maven project

In the project `pom.xml` file, add `testable-all` dependency and `maven-surefire-plugin` configuration, the specific method is as follows.

It is recommended to add a `property` field that identifies the TestableMock version, in order to simplify version management:

```xml
<properties>
    <testable.version>0.4.7</testable.version>
</properties>
```

Add dependence of `TestableMock` inside `dependencies` field:

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

Finally, add the `maven-surefire-plugin` plugin to the `plugins` list in the `build` area (if this plugin is already included, just add the `<argLine>` part of the configuration):

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

If the project also uses the `on-the-fly` mode of `Jacoco` (default mode) to calculate the unit test coverage, you need to add a `@{argLine}` parameter in the `<argLine>` configuration, after adding it The configuration is as follows:

```xml
<argLine>@{argLine} -javaagent:${settings.localRepository}/com/alibaba/testable/testable-agent/${testable.version}/testable-agent-${testable.version}.jar</argLine>
```

See the [pom.xml](https://github.com/alibaba/testable-mock/blob/master/demo/java-demo/pom.xml) file of project `java-demo` and the [pom.xml](https://github.com/alibaba/testable-mock/blob/master/demo/kotlin-demo/pom.xml) file of project `kotlin-demo`.

## Use in Gradle project

Add dependence of `TestableMock` in `build.gradle` file:

```groovy
dependencies {
    testImplementation('com.alibaba.testable:testable-all:0.4.7')
    testAnnotationProcessor('com.alibaba.testable:testable-processor:0.4.7')
}
```

Then add javaagent to `test` configuration：

```groovy
test {
    jvmArgs "-javaagent:${classpath.find { it.name.contains("testable-agent") }.absolutePath}"
}
```

See the [build.gradle](https://github.com/alibaba/testable-mock/blob/master/demo/java-demo/build.gradle) file of project `java-demo` and the [build.gradle.kts](https://github.com/alibaba/testable-mock/blob/master/demo/kotlin-demo/build.gradle.kts) file of project `kotlin-demo`.
