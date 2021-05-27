# Release Note

## 0.6.6
- 支持全局参数配置Mock容器的包路径映射，便于Mock三方包内的代码（issue-105）
- 实现Mock功能的防代码重构机制（issue-5）
- 解决Mock类的调试日志内容有时会错位的问题
- 修复`OmniConstructor`在某些版本JVM下构造`Long`和`Integer`类报错的问题

## 0.6.5
- 修复`OmniConstructor`对某些系统类型的兼容问题（issue-145）
- 修复`PrivateAccessor`无法调用仅有数组类型参数方法的问题（issue-152）
- 修复当`scope`为`associated`时父类中的Mock方法不生效问题（issue-139）
- 修复当`scope`为`associated`时空白的Mock方法会报错的问题（issue-154）

## 0.6.4
- 移除`TestableNull`类型，让`OmniConstructor`更轻量
- 支持JUnit 5的`@Nested`注解（issue-140）
- 修复多处`OmniConstructor`的兼容性BUG（感谢[@ddatsh](https://github.com/ddatsh)）

## 0.6.3
- 支持自定义内部Mock容器类名（issue-137）
- 支持自定义`OmniConstructor`字节码增强范围
- 修复两处可导致`OmniConstructor`无限递归构建的问题（包括issue-138）
- 修复一处非静态方法获取`this`引用失败导致的下标越界异常（issue-136）
- 修复获取构建目录失败导致的空指针异常（issue-135）

## 0.6.2
- 支持通过Properties文件配置TestableAgent
- 默认禁用OmniConstructor相关的字节码增强
- 修复OmniConstructor与`Spring`框架的兼容问题（issue-129）

## 0.6.1
- 自动生成Mock扫描过程日志文件，便于自助排查问题
- 修复一处`Spock`测试框架的兼容问题 (issue-121)
- 修复一处`Gradle`运行单元测试的兼容问题（issue-123）

## 0.6.0
- 增加`OmniConstructor`和`OmniAccessor`，支持快速构建方法入参
- 修复一处`FRAME FULL`字节码处理的异常 (issue-117)
- 移除`@MockWith`的`diagnose`参数支持

## 0.5.2
- 支持使用`PrivateAccessor`访问父类中的私有成员 (issue-91)
- 修复在较高版本JVM下的非法类型错误 (issue-112)
- 修复Mock目标为接口类型时的字节码异常 (issue-82)

## 0.5.1
- 在`VERBOSE`级别诊断日志中使用更易于阅读的方法签名格式
- 增加对JUnit5参数化测试的支持 (issue-98)
- 修复在调用参数中包含三元表达式时的Mock异常（issue-92）
- 修复使用`@MockWith`时，日志输出丢失的BUG (issue-99)

## 0.5.0
- 分离测试类与Mock类，实现Mock类和Mock方法的复用机制
- 支持测试类与被测类在不同包路径的情况下实施Mock
- 支持将Mock方法生效范围缩小为所属测试类的测试用例
- 使用`TransmittableThreadLocal`替换基于线程堆栈的Mock上下文识别机制
- 增加专用于输出诊断信息的`@MockDiagnose`注解

## 0.4.12
- 支持`VERBOSE`级别的Mocking过程日志，增强错误自助排查能力
- 支持使用`verifyTargetOnCompile`参数禁用编译期私有目标校验功能
- 支持通过agent参数指定Mock目标的扫描包范围
- 修复一处`ArrayIndexOutOfBoundsException`异常 (issue-52）

## 0.4.11
- 支持测试类访问与自身包路径不同的被测类的私有成员
- 增加`PrivateAccessor`访问的私有方法参数数目检查，提高抗代码重构能力
- 修复被Mock方法包含数组参数可能导致出错的BUG (issue-48)
- 修复一处会导致在IntelliJ中构建找不到私有成员的问题

## 0.4.10
- 修复在Lambda函数中使用Mock出错的BUG（issue-44）
- 修复调用私有方法时参数值不能为null的问题（issue-27）

## 0.4.9
- 修复发起调用的对象不是局部或成员变量时Mock出错的BUG (issue-40)
- 增加`PrivateAccessor`访问目标有效性检查，提高抗代码重构能力 (issue-21)

## 0.4.8
- 修复赋值语句中的私有方法调用无法访问的BUG (issue-33)
- 支持在包含`SpringRunner`的测试中使用Mock (issue-30)
- `@MockMethod`注解支持`targetClass`参数 (issue-24)

## 0.4.7
- 修复由于`MOCK_CONTEXT`引入的堆栈大小错误
- 修复Windows下的`Gradle`构建路径错误 (issue-25)

## 0.4.6
- 修复一处`IINC`字节码处理异常
- 支持使用`TestableTool.MOCK_CONTEXT`变量为Mock方法注入额外上下文参数 (issue-17)
- 不再推荐使用`TestableTool.TEST_CASE`变量来区分测试用例

## 0.4.5
- 修复IntelliJ 2020.3+环境下的私有成员访问编译期错误
- 修复潜在的跨用例初始化空指针异常 (issue-20)
- 支持将运行期修改后的字节码Dump到本地文件

## 0.4.4
- 修复无法访问参数类型包含接口的私有方法的BUG (issue-15)
- 修复Mock无参数的静态方法会出错的BUG (issue-16)

## 0.4.3
- 完善了对私有静态成员的直接访问能力

## 0.4.2
- 支持通过Maven插件设置`TestableAgent`的全局日志级别
- 修复测试类可能被重复注入多余字节码的BUG
- 修复一处导致跨行的方法调用Mock报错的BUG

## 0.4.1
- `@TestableMock`注解已弃用，推荐使用`@MockMethod`和`@MockConstructor`注解

## 0.4.0
- 修复由于默认类加载器变化导致JVM 9+运行时Mock出错的BUG
- 修复多个测试类包含同名Mock方法时潜在的串号问题
- 重构项目结构，增加用于简化依赖配置的`testable-all`模块
- `TestableTool`中与用户无关的内容迁移到了`TestableConst`类型

## 0.3.2
- 支持在Gradle项目中使用私有成员访问和快速Mock功能
- 支持通过`PrivateAccessor`工具类访问私有静态成员

## 0.3.1
- 支持使用`@MockWith`注解打印详细Mock执行过程

## v0.3.0
- 增加`without()`校验器用于匹配指定Mock方法从未被调用的场景
- 支持对Mock调用参数验证时使用模糊匹配

## v0.2.2
- 支持对Mock调用参数进行校验
- 修复JVM 9+的兼容性问题

## v0.2.1
- 支持Mock静态方法
- 支持Mock Kotlin Companion对象的方法
- 支持Mock接口或父类引用指向子类实例时的调用

## v0.2.0
- 支持通过`TestableTool`工具类识别当前的调用上下文
- 增加`testable-maven-plugin`模块用于简化运行时JavaAgent配置
- 不再需要使用`@EnableTestable`注释显式标记测试类
- 重名了现有的各种注解，以便与更好的与实际功能对应

## v0.1.0
- 将生成的Agent Jar包自动移到打包工程的Class目录
- 支持Mock任意类型的方法调用（不再局限于被测类中的方法）

## v0.0.5
- 使用运行期字节码修改替代预先生成的`e.java`文件
- 消除所有对特定单元测试框架的定制逻辑
- 将测试类中的Testable引用字段从编译期生成改为运行期生成

## v0.0.4
- 改用运行期字节码修改来调用Testable的`setup`方法
- 添加`TestableUtil`工具类用于获取当前测试用例和调用来源

## v0.0.3
- 使用静态方法封装私有成员的访问过程，不在原调用处直接添加反射代码
- 使用`e.java`文件替代`testable`类型，提高代码可读性
- 引入`agent`模块，使用运行期字节码修改实现`new`操作和私有成员调用的Mock

## v0.0.2
- 基于编译期代码修改支持Mock私有成员方法的调用

## v0.0.1
- 首个概念验证版本
- 基于编译期代码修改支持`new`操作的Mock和访问被测类私有成员
