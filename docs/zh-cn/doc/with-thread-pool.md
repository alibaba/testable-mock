Mock线程池内的调用
---

`TestableMock`采用来自[transmittable-thread-local](https://github.com/alibaba/transmittable-thread-local)项目的`TransmittableThreadLocal`类型存储测试用例运行期的`MOCK_CONTEXT`内容和Mock方法调用过程。

当线程池中的执行对象未经过`TtlRunnable`或`TtlCallable`处理时，`TransmittableThreadLocal`将自动降级为与`InheritableThreadLocal`等效的类型，即只对父子线程有效，无法在线程池上下文中正常传递存储数据。因而会导致`MOCK_CONTEXT`内容丢失和`verify()`方法校验结果不正确的情况。

为此，可以启用[Testable全局配置](zh-cn/doc/javaagent-args.md)`thread.pool.enhance.enable=true`，来自动在测试启动时自动封装程序中的普通`Runnable`和`Callable`对象，使`TransmittableThreadLocal`恢复跨线程池存储数据的能力。

同时还需要配合修改项目`pom.xml`或`build.gradle`文件，将`transmittable-thread-local`中的类型增加到`TestableMock`运行的Classpath里，具体方法如下。

### 使用Maven构建

首先增加一个属性，以便将来修改该依赖的版本值：

```xml
<properties>
    <transmittable.thread.local.version>2.12.1</transmittable.thread.local.version>
</properties>
```

然后，在`maven-surefire-plugin`插件的`argLine`参数中添加运行参数`-Xbootclasspath/a:${settings.localRepository}/com/alibaba/transmittable-thread-local/${transmittable.thread.local.version}/transmittable-thread-local-${transmittable.thread.local.version}.jar`。

添加后的完整`maven-surefire-plugin`配置参考：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <configuration>
                <argLine>-Xbootclasspath/a:${settings.localRepository}/com/alibaba/transmittable-thread-local/${transmittable.thread.local.version}/transmittable-thread-local-${transmittable.thread.local.version}.jar -javaagent:${settings.localRepository}/com/alibaba/testable/testable-agent/${testable.version}/testable-agent-${testable.version}.jar</argLine>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 使用Gradle构建

在`test`区块内添加测试参数`jvmArgs "-Xbootclasspath/a:${classpath.find { it.name.contains("transmittable-thread-local") }.absolutePath}"`

添加后的完整`test`区块配置参考：

```groovy
test {
  jvmArgs "-javaagent:${classpath.find { it.name.contains("testable-agent") }.absolutePath}"
  jvmArgs "-Xbootclasspath/a:${classpath.find { it.name.contains("transmittable-thread-local") }.absolutePath}"
  ... // 其他测试配置
}
```
