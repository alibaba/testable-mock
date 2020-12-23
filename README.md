# TestableMock

换种思路写Mock，让单元测试更简单。

无需初始化，不挑测试框架，甭管要换的是私有方法、静态方法、构造方法还是其他任何类的任何方法，也甭管要换的对象是怎么创建的。写好Mock定义，加个`@MockMethod`注解，一切统统搞定。

- 文档：https://alibaba.github.io/testable-mock/
- 国内文档镜像：http://freyrlin.gitee.io/testable-mock/ （速度快，内容稍有延迟）

阅读[这里](https://mp.weixin.qq.com/s/KyU6Eu7mDkZU8FspfSqfMw)了解更多故事。

## 目录结构

```bash
|-- testable-parent       ➜ 提供各子模块的公共父pom文件
|-- testable-all          ➜ 依赖聚合，便于一次性引用所有子模块功能
|-- testable-processor    ➜ 编译期代码预处理模块，提供测试辅助功能
|-- testable-agent        ➜ JavaAgent模块，提供Mock测试相关功能
|-- testable-core         ➜ 基础功能模块，提供Mock相关注解和工具类
|-- testable-maven-plugin ➜ Maven插件模块，用于简化JavaAgent注入
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

## 本地生成文档

```bash
docsify serve docs
```

> TestableMock文档使用`docsify`工具生成，构建前请安装[nodejs](https://nodejs.org/en/download/)运行时，并使用`npm install -g docsify`命令安装文档生成工具。

