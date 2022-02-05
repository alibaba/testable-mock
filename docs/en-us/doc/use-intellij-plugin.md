Use IntelliJ Plugin
---

There are some typical operations in the process of using`TestableMock` library, such as define a mock class, copy signature of the method invoked and convert it to mock method definition. While the mock result does not match expectation, we have to debug the mock affection via [Self-Help Troubleshooting](zh-cn/doc/troubleshooting.md) guide during runtime. With the help of IDE plugin, part of the patterned operations can be automatically completed to further optimize the experience of `TestableMock`. 

Currently `TestableMock` provided only [IntelliJ IDEA](https://www.jetbrains.com/idea/) plugin. (According to the [2020](https://www.jrebel.com/blog/2020-java-technology-report#IDE) and [2021](https://www.jrebel.com/blog/2021-java-technology-report) Java Technology Trends Report released by JRebel, IntelliJ IDEA has become the IDE of choice with the highest percentage of Java developers)

## Usage

Open preference dialog, search "Testable-Mock" in plugin market, reach `Testable-Mock Helper` plugin and click "install", then restart IDE.

![install](https://img.alicdn.com/imgextra/i2/O1CN013twjqN2803NVtvJEP_!!6000000007869-0-tps-2342-566.jpg)

#### 1. **Generate Mock Class**

After plugin installed, right click in any java class, choose "Generate Testable TestClass"

![mock-class](https://img.alicdn.com/imgextra/i1/O1CN01gNsunh21jfVufv6EU_!!6000000007021-0-tps-546-291.jpg)

A test class containing a mock container class will be automatically generated in the same package path location as the current class in the `test` directory of the project (named after `CurrentClass + Test`)

#### 2. **Create Mock Method**

Select any method invocation, right click and choose "Copy Testable Mock-Method" or "Copy Mock-Method To TestClass"

![mock-method](https://img.alicdn.com/imgextra/i4/O1CN01K38Zsh1NrNYLlrUZ9_!!6000000001623-0-tps-642-264.jpg)

The former will convert the method signature of selected invocation into a mock method definition and store it on the clipboard; the latter will directly insert the converted mock method into the corresponding mock class.

## Build from source code

Beside install plugin from market, you can also build it from source.

Make sure you have jdk 11 installed, execute below command:

```bash
git clone https://github.com/zcbbpo/testable-idea
cd testable-idea
./gradlew clean build
```

After build, the packaged zip file is lay in `build/distributions/` folder, you can install it by "Install Plugin From Disk" menu.

![from-local](https://img.alicdn.com/imgextra/i3/O1CN01YMHefk26MNS4pH4ZI_!!6000000007647-0-tps-2342-516.jpg)

Finally, special thanks to the contribution of `TestableMock` IntelliJ IDEA plugin made by @[zcbbpo](https://github.com/zcbbpo).
