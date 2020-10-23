使用说明
---

## 引入Testable

首先在项目`pom.xml`文件中添加`testable-core`依赖：

```xml
<dependency>
    <groupId>com.alibaba.testable</groupId>
    <artifactId>testable-core</artifactId>
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

在测试类上添加`@EnableTestable`注解，然后即可直接在单元测试里像访问公有成员一样调用被测类的私有方法、读写私有成员变量了，虽然IDE可能会提升语法有误，但编译器将会正常运行测试。

若不希望看到IDE的语法错误提醒，或是在基于JVM的非Java语言项目里（譬如Kotlin语言），也可以借助`PrivateAccessor`工具类来实现私有成员的访问。

效果见示例项目文件`DemoServiceTest.java`中的`should_able_to_test_private_method()`和`should_able_to_test_private_field()`测试用例。

### Mock被测类的任意方法调用

**【1】覆写任意类的方法调用**

定义一个普通方法，使它与待覆写方法名称和返回值类型完全一致，仅比待覆写方法在首位多一个该方法所属对象类型的参数，然后为这个方法加上`@TestableInject`注解，并设置`targetClass`属性值为被Mock方法的所属类型。

此时被测类中所有对该类指定方法的调用，将在单元测试运行时，自动被替换为对上述自定义Mock方法的调用。

`@TestableInject`注解还有一个很少需要用到的`targetMethod`属性，用于指定Mock的目标方法名称。使用此参数后被注释修饰的方法名称就可以随意命名了，通常仅在遇到极其罕见的Mock方法签名重名情况时才需要使用。

示例项目文件`DemoServiceTest.java`中的`should_able_to_test_common_method()`用例详细展示了这几种用法。

**【2】覆写任意类的new操作**

同样还是定义一个普通方法，然后加上`@TestableInject`注解。方法名称随意，只需让方法的返回值为要覆写new操作的目标类型，且参数与指定类构造方法完全一致。

此时被测类中所有用`new`创建指定类的操作将被替换为对该自定义方法的调用。

详见示例项目文件`DemoServiceTest.java`中的`should_able_to_test_new_object()`用例。

**【3】覆写被测类自身的私有成员方法**

有时候，被测类自身的某个成员方法访问了外部系统，在进行单元测试的时候就需要将这个备查样自己的成员方法Mock掉。

在测试类中声明一个名称、参数和返回值类型都与要覆写的目标方法完全一致的普通方法，同样加上`@TestableInject`注解，不配置`targetClass`属性，即可实现对被测类私有成员方法的覆写。

详见示例项目文件`DemoServiceTest.java`中的`should_able_to_test_member_method()`用例。

**【4】识别当前测试用例和调用来源**

在Mock方法中可以通过`TestableTool.TEST_CASE`和`TestableTool.SOURCE_METHOD`来识别**当前运行的测试用例名称**和**进入该Mock方法前的被测类方法名称**，从而区分处理不同的调用场景。

详见示例项目文件`DemoServiceTest.java`中的`should_able_to_get_source_method_name()`和`should_able_to_get_test_case_name()`用例。
