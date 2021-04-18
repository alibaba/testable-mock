快速构造复杂的参数对象
---

在单元测试中，测试数据的准备和构造是一件既必须又繁琐的任务，面向对象的层层封装，在测试时就成为了初始化对象状态的重重阻碍。尤其遇到类型结构嵌套复杂、没有合适的构造方法、需要使用私有内部类等等状况时，常规手段往往显得力不从心。

为此`TestableMock`提供了`OmniConstructor`和`OmniAccessor`两个极简的工具类，从此让一切对象构造不再困难。

### 1. 一行代码构造任何对象

不论目标类型多么奇葩，呼唤`OmniConstructor`，马上递给您~ 万能的对象构造器`OmniConstructor`有两个静态方法：

- `newInstance(任意类型)` ➜ 指定任意类型，返回一个该类型的对象
- `newArray(任意类型, 数组大小)` ➜ 指定任意类型，返回一个该类型的数组

用法举例：

```java
// 构造一个WhatEverClass类型的对象
WhatEverClass obj = OmniConstructor.newInstance(WhatEverClass.class);
// 构造一个WhatEverClass[]类型，容量为5的数组
WhatEverClass[] arr = OmniConstructor.newArray(WhatEverClass.class, 5);
```

不仅如此，`OmniConstructor`构造出的绝非是所有成员值为`null`的简单空对象，而是所有成员、以及所有成员的所有子成员，都已经依次递归初始化的"丰满"对象。相比使用`new`进行构造，`OmniConstructor`能够确保对象结构完整，避免测试数据部分初始化导致的`NullPointerException`问题。

```java
// 使用构造函数创建对象
Parent parent = new Parent();
// 内部成员未初始化，直接访问报NullPointerException异常（❌）
parent.getChild().getGrandChild();

// 使用OmniConstructor创建对象
Parent parent = OmniConstructor.newInstance(Parent.class);
// 无需顾虑，安心访问任意子成员（✅）
parent.getChild().getGrandChild().getContent();
```

> **注意 1** ：在当前版本中，类型为接口或抽象类的成员字段依然会被初始化为`null`，此问题将在后续版本修复
>
> **注意 2** ：基于轻量优先原则，默认模式下，`OmniConstructor`仅利用类型原有的构造方法来创建对象，对于绝大多数POJO和`Model`层对象而言这种模式已经能够满足需要。
> 但对于更复杂的情形，譬如遇到某些类型的构造方法会抛出异常或包含其他妨碍构造正常执行的语句时，对象构造可能会失败。
> 此时可通过[Testable全局配置](zh-cn/doc/javaagent-args.md)`omni.constructor.enhance.enable = true`启用`OmniConstructor`的字节码增强模式，在该模式下，任何Java类型皆可构造。

除了用于构造方法的入参，`OmniConstructor`也可以用于快速构造Mock方法的返回值，相比将`null`作为Mock方法的返回值，使用完全初始化的对象能够更好保障测试的可靠性。

在`java-demo`和`kotlin-demo`示例项目的`DemoOmniMethodsTest`测试类中，详细展示了当目标类型有多层嵌套结构、构造方法无法正常使用，甚至没有公开的构造方法时，如何用`OmniConstructor`轻松创建所需对象。

### 2. 一行代码访问任意深度成员

对于测试数据而言，即使是结构复杂的参数对象，与特定测试用例有关的通常也只是其中的部分属性和状态，然而要为这些深藏在对象结构内部的字段赋值有时却并非易事。

做为`PrivateAccessor`功能的加加加强版，`OmniAccessor`的灵感来自于`XML`语言中的[XPath节点选择器](https://www.w3school.com.cn/xpath/xpath_syntax.asp)，它提供了`get`、`set`两个主要的静态方法：

- `get(任意对象, "访问路径")` ➜ 返回根据路径匹配搜索到的所有成员对象
- `set(任意对象, "访问路径", 新的值)` ➜ 根据路径匹配为指定位置的对象赋值

还有一个用于精确路径匹配时直接获取唯一目标对象的`getFirst()`辅助方法，其作用等效于`OmniAccessor.get(...).get(0)`：

- `getFirst(任意对象, "访问路径")` ➜ 返回根据路径匹配搜索到的第一个成员对象

只需书写符合规则的访问路径，不论什么类型和深度的成员，都可以一键直达：

```java
// 返回parent对象中，所有符合类型是GrandChild的子对象中叫做content的成员对象
OmniAccessor.get(parent, "{GrandChild}/content");
// 将parent对象中，符合名称为children的数组第3位的任意子成员的value字段赋值为100
OmniAccessor.set(parent, "children[2]/*/value", 100);
```

具体路径规则如下：

**1. 匹配成员名**

不带额外修饰的路径名将匹配与之同名的任意成员对象

- `child`: 匹配任意名字为`child`的子孙成员
- `child/grandChild`: 匹配名字为`child`的子孙成员里，名为`grandChild`的子成员

**2. 匹配成员类型**

使用花括号匹配类型名称，通常用于批量获取或赋值同类的多个成员对象

- `{Child}`: 匹配所有类型是`Child`的子孙成员
- `{Children[]}`: 匹配所有类型是`Children`数组的子孙成员
- `{Child}/{GrandChild}`: 匹配所有类型是`Child`的子孙成员里，所有类型是`GrandChild`子成员

成员名和类型可以在路径上混用（暂不支持在同一级路径同时指定成员名称和类型）

- `child/{GrandChild}`: 匹配名字为`child`的子孙成员里，所有类型是`GrandChild`的子成员
- `{Child}/grandChild/content`: 匹配所有类型是`Child`的子孙成员里，名为`grandChild`子成员里的，名为`content`的子成员

**3. 使用下标访问数组成员**

使用带数值的方括号表示匹配该位置为数组类型，且取指定下标的对象（不带下标时，当匹配对象为数组类型，默认匹配数组中的所有对象）

- `children[1]/content`: 匹配名称为`children`的数组类型子孙成员，取其中第`2`个对象中名为`content`的子成员
- `parent/children[1]`: 匹配名称为`parent`的子孙成员里，名为`children`的数组类型子成员，取其中第`2`个对象

**4. 使用通配符**

通配符可以用于成员名或类型名的匹配

- `child*`: 匹配名称以`child`开头的所有子孙成员
- `{*Child}`: 匹配类型以`Child`结尾的所有子孙成员
- `c*ld/{Grand*ld}`: 匹配名称以`c`开头`ld`结尾的子孙成员里，类型以`Grand`开头`ld`结尾的成员
- `child/*/content`: 此时`*`将匹配任意成员，即`child`对象任意子成员中，包含的`content`子成员

详见`java-demo`和`kotlin-demo`示例项目`DemoOmniMethodsTest`测试类中的用例。

### 3. 特别说明

> **你真的需要用到`OmniAccessor`吗？**
>
> `OmniAccessor`具有基于Fail-Fast机制的防代码重构能力，当用户提供的访问路径无法匹配到任何成员时，`OmniAccessor`将立即抛出`NoSuchMemberError`错误，使单元测试提前终止。然而相比常规的成员访问方式，`OmniAccessor`在IDE重构方面的支持依然偏弱。
>
> 对于复杂对象的内容赋值，大多数情况下，我们更推荐使用[构造者模式](https://developer.aliyun.com/article/705058)，或者暴露Getter/Setter方法实现。这些常规手段虽然稍显笨拙（尤其在需要为许多相似的成员批量赋值的时候），但对业务逻辑的封装和重构都更加友好。
> 仅当原类型不适合改造，且没有其它可访问目标成员的方法时，`OmniAccessor`才是最后的终极手段。
