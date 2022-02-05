使用IntelliJ IDEA插件
---

在使用`TestableMock`工具的过程中经常涉及一些固定操作，比如定义Mock容器类、复制需Mock的调用定义并创建Mock方法，在执行测试时若发现Mock匹配的效果不符合预期，则需要通过[自助问题排查](zh-cn/doc/troubleshooting.md)方法在运行期进行校验。通过IDE插件辅助，能够将部分模式化的操作自动完成，进一步优化`TestableMock`的使用体验。

目前`TestableMock`提供了 [IntelliJ IDEA](https://www.jetbrains.com/idea/) 的插件支持（根据JRebel发布的 [2020](https://www.jrebel.com/blog/2020-java-technology-report#IDE) 和 [2021](https://www.jrebel.com/blog/2021-java-technology-report) Java技术趋势报告，IntelliJ IDEA已成为Java开发者使用比例最高的首选IDE）。

## 使用方法

打开配置项，在插件市场中搜索“Testable-Mock”，选中`Testable-Mock Helper`插件，点击“安装”，然后重启IDE。

![install](https://img.alicdn.com/imgextra/i2/O1CN013twjqN2803NVtvJEP_!!6000000007869-0-tps-2342-566.jpg)

#### 1. **创建Mock容器类**

安装完插件后，在任意Java类点击右键，点击“生成Testable测试类”（英文版为“Generate Testable TestClass”）

![mock-class](https://img.alicdn.com/imgextra/i1/O1CN01gNsunh21jfVufv6EU_!!6000000007021-0-tps-546-291.jpg)

在项目`test`目录下与当前类相同的包路径位置会自动生成一个包含Mock容器类的测试类（已`当前类+Test`命名）。

#### 2. **创建Mock方法**

选中任意方法调用，点击右键，点击“复制方法为Testable-Mock”（英文版为“Copy Testable Mock-Method”）或“复制Testable-Mock方法到测试类”（英文版为“Copy Mock-Method To TestClass”）

![mock-method](https://img.alicdn.com/imgextra/i4/O1CN01K38Zsh1NrNYLlrUZ9_!!6000000001623-0-tps-642-264.jpg)

前者会将被选中调用的方法签名，并转换为Mock方法定义存放到剪贴板；后者则会将转换好的Mock方法直接插入到相应的Mock类里。

## 源码构建

除了通过插件市场安装，也可以直接从源码构建`Testable-Mock Helper`插件。

本地准备JDK 11环境，然后执行以下命令：

```text
git clone https://github.com/zcbbpo/testable-idea
cd testable-idea
./gradlew clean build
```

构建完成后，在`build/distributions/`目录下可以找到构建好的插件zip包，通过“从本地磁盘安装”菜单加载该插件。

![from-local](https://img.alicdn.com/imgextra/i3/O1CN01YMHefk26MNS4pH4ZI_!!6000000007647-0-tps-2342-516.jpg)

在此，特别感谢 @[zcbbpo](https://github.com/zcbbpo) 对`TestableMock`IntelliJ IDEA插件的贡献。
