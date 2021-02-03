在IDE中运行单元测试
---

## 使用IntelliJ IDE

IntelliJ IDE对`TestableMock`所用到的`JSR-269`注释处理器以及`maven-surefire-plugin`插件的附加参数均支持良好。通常无需特殊配置，可开箱即用。

## 使用Eclipse IDE

由于`Eclipse`内置的自动编译功能基于三方编译器实现，与标准`javac`编译过程不兼容，会导致在IDE中运行测试用例时`@EnablePrivateAccess`注解无效。不过，通过`PrivateAccessor`工具类访问被测类私有成员的功能不会受编译器差异影响。

若项目中使用了`@EnablePrivateAccess`注解，可在`Eclipse`的命令行中使用`mvn test -Dtest=<测试类名>`和`mvn test -Dtest=<测试类名>#<测试用例名>`来运行单个测试类或测试用例。

同时，由于`Eclipse`内置的单元测试执行器完全忽略`pom.xml`文件的配置，因此若需使用Mock功能，需进行额外配置。

以使用`JUnit`为例，方法为从IDE工具栏的运行按钮旁边的小三角处下拉，选择"Run Configurations..."，左侧选择要运行单元测试的任务，在右侧切换到"arguments"标签页，在"VM Options"里添加`-javaagent:`参数，下图为示例，注意应修改`testable-agent`包为与实际情况匹配的本地Maven仓库路径。

![eclipse-junit-configuration](https://testable-code.oss-cn-beijing.aliyuncs.com/eclipse-junit-configuration.png)
