使用说明
---

## 引入Testable

首先在项目`pom.xml`文件中添加`testable-processor`依赖：

```xml
<dependency>
    <groupId>com.alibaba.testable</groupId>
    <artifactId>testable-processor</artifactId>
    <version>${testable.version}</version>
    <scope>provided</scope>
</dependency>
```

此时项目就获得了在单元测试中随意访问被测类私有字段和方法的能力（需配合注解使用，见下文详述）。

若要开启极速Mock功能，还需在`pom.xml`里加上`testable-maven-plugin`插件。

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

## 使用Testable

`Testable`目前能为测试类提供两项增强能力：__直接访问被测类的私有成员__ 和 __极速Mock被测方法中的调用__

### 访问私有成员字段和方法

只需为测试类添加`@EnablePrivateAccess`注解，即可在测试用例中获得以下增强能力：

- 调用被测类的私有方法
- 读取被测类的私有成员
- 修改被测类的私有成员
- 修改被测类的常量成员（使用final或static final修饰的成员）

访问和修改私有、常量成员时，IDE可能会提示语法有误，但编译器将能够正常运行测试。

若不希望看到IDE的语法错误提醒，或是在基于JVM的非Java语言项目里（譬如Kotlin语言），也可以借助`PrivateAccessor`工具类来实现私有成员的访问。

效果见示例项目文件`DemoServiceTest.java`中的`should_able_to_mock_private_method()`和`should_able_to_mock_private_field()`测试用例。

### Mock被测类的任意方法调用

**【1】覆写任意类的方法调用**

在测试类里定义一个有`@TestableMock`注解的普通方法，使它与需覆写的方法名称、参数、返回值类型完全一致，然后在其参数列表首位再增加一个类型为该方法原本所属对象类型的参数。

此时被测类中所有对该需覆写方法的调用，将在单元测试运行时，将自动被替换为对上述自定义Mock方法的调用。

**注意**：当遇到有两个需覆写的方法重名时，可将需覆写的方法名写到`@TestableMock`注解的`targetMethod`参数里，此时Mock方法自身就可以随意命名了。

示例项目文件`DemoServiceTest.java`中的`should_able_to_mock_common_method()`用例详细展示了这种用法。

**【2】覆写被测类自身的成员方法**

有时候，在对某些方法进行测试时，希望将被测类自身的另外一些成员方法Mock掉。

操作方法与前一种情况相同，Mock方法的第一个参数类型需与被测类相同，即可实现对被测类自身（不论是公有或私有）成员方法的覆写。

详见示例项目文件`DemoServiceTest.java`中的`should_able_to_mock_member_method()`用例。

**【3】覆写任意类的new操作**

在测试类里定义一个有`@TestableMock`注解的普通方法，将注解的`targetMethod`参数写为"<init>"，然后使该方法与要被创建类型的构造函数参数、返回值类型完全一致，方法名称随意。

此时被测类中所有用`new`创建指定类的操作（并使用了与Mock方法参数一致的构造函数）将被替换为对该自定义方法的调用。

详见示例项目文件`DemoServiceTest.java`中的`should_able_to_mock_new_object()`用例。

**【4】识别当前测试用例和调用来源**

在Mock方法中可以通过`TestableTool.TEST_CASE`和`TestableTool.SOURCE_METHOD`来识别**当前运行的测试用例名称**和**进入该Mock方法前的被测类方法名称**，从而区分处理不同的调用场景。

详见示例项目文件`DemoServiceTest.java`中的`should_able_to_get_source_method_name()`和`should_able_to_get_test_case_name()`用例。
