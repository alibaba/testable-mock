Testable Maven Plugin
---

For projects built with Maven, in addition to directly modifying the argument of the `maven-surefire-plugin` plugin, the same effect can also be obtained through the `testable-maven-plugin` plugin:

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

> When using `testable-maven-plugin`, the `TestableMock` related configuration on `maven-surefire-plugin` should be removed.

The `testable-maven-plugin` can be used with the Jacoco plugin without additional adaptation, so it can make the writing of the `pom.xml` file easier and more readable.

However, it should be noted that when running a single test case through the IDE, those mock function may fail to work.

This is because the IDE usually only runs the `maven-surefire-plugin` plugin when running a single test case, skipping the execution of the `testable-maven-plugin` plugin, resulting in the JavaAgent required for the mock function not being injected into the context.

This problem can be bypassed by configuring the test parameters of the IDE additionally. Take IntelliJ as an example, open the "Edit Configuration..." option of the run menu, as shown in the position ①

![modify-run-configuration](https://testable-code.oss-cn-beijing.aliyuncs.com/modify-run-configuration.png)

Add JavaAgent startup parameters at the end of the "virtual machine parameters" attribute value: `-javaagent:${HOME}/.m2/repository/com/alibaba/testable/testable-agent/xyz/testable-agent-xyzjar`, as shown in the figure position ②

> PS: Please replace `x.y.z` in the path with the actual version number

![add-testable-javaagent](https://testable-code.oss-cn-beijing.aliyuncs.com/add-testable-javaagent.png)

Finally, click to run the unit test, as shown in the position ③

In general, the additional complexity of using `testable-maven-plugin` is still higher than its simplified configuration. At present, modify the configuration of the `maven-surefire-plugin` directly in the `pom.xml` file is still the preferred solution.
