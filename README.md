# TestableMock

基于代码和字节码增强的Java单元测试辅助工具，包含以下功能：

- 使单元测试能直接调用和访问被测类的私有成员，解决私有方法无法测试的问题
- 使被测类的任意方法调用快速替换为Mock，实现"指哪换哪"，解决传统Mock工具使用繁琐的问题

## 目录结构

```bash
|-- testable-core         ➜ 核心组件，提供注解和工具类
|-- testable-processor    ➜ 编译期代码预处理组件，提供测试辅助功能
|-- testable-agent        ➜ JavaAgent组件，提供Mock测试相关功能
|-- testable-maven-plugin ➜ Maven插件组件，用于简化JavaAgent注入
|-- demo
|   |-- java-demo         ➜ Java语言的示例代码
|   `-- kotlin-demo       ➜ Kotlin语言的示例代码
`-- docs                  ➜ 项目使用文档
```

## 构建项目

主项目使用JDK 1.6+和Maven 3+版本构建，其中`demo`子项目需要JDK 1.8+版本。

```bash
mvn clean install
```
