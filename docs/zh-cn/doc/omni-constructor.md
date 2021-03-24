快速构造复杂的参数对象
---

在单元测试中，测试数据的准备和构造是一件既必须又繁琐的任务，尤其遇到被测函数的入参类型结构复杂、没有合适的构造方法、成员对象使用私有内部类的时候，常规方法往往无处下手。为此`TestableMock`提供了`OmniConstructor`和`OmniAccessor`两个极简的工具类，从此让一切对象构造不再困难。

### 1. 一行代码构造任何对象

万能的对象构造器`OmniConstructor`有两个静态方法：

- `newInstance(任意类型)` ➜ 指定任意类型，返回一个该类型的对象
- `newArray(任意类型, 数组大小)` ➜ 指定任意类型，返回一个该类型的数组

例如：

```java
// 构造一个ComplicatedClass类型的对象
ComplicatedClass obj = OmniConstructor.newInstance(ComplicatedClass.class);
// 构造一个ComplicatedClass[]类型，容量为5的数组
ComplicatedClass[] arr = OmniConstructor.newArray(ComplicatedClass.class, 5);
```

值得一提的是，使用`OmniConstructor`构造出来的并非是一个所有成员值为`null`的简单空对象。该对象的所有成员，以及所有成员的所有子成员，都会在构造时被依次递归赋值。相比直接用`new`构造的对象，使用`OmniConstructor`能够确保对象完全初始化，无需担心测试过程中发生`NullPointerException`问题。

> **注意**：在`0.6.0`版本中，类型为接口或抽象类的成员字段依然会被初始化为`null`，此问题将在近期版本修复

```java
// 使用构造函数创建对象
Parent parent = new Parent();
// 内部成员未初始化，直接访问报NullPointerException异常
parent.getChild().getGrandChild();

// 使用OmniConstructor创建对象
Parent parent = OmniConstructor.newInstance(Parent.class);
// 无需顾虑，安心访问任意子成员
parent.getChild().getGrandChild().getContent();
```

除了用于构造方法的入参，`OmniConstructor`也可以用于快速构造Mock方法的返回值，相比将`null`作为Mock方法的返回值，使用完全初始化的对象能够更好保障测试的可靠性。

详见`java-demo`和`kotlin-demo`示例项目`DemoOmniMethodsTest`测试类中的用例。

### 2. 一行代码访问任意深度成员

在单元测试中，有时会遇到一些结构复杂的参数对象，但与特定测试用例有关的仅仅是该对象结构深处的个别几个属性和状态。`OmniAccessor`的灵感来自于`XML`语言中的`xpath`节点选择器，它有`get`、`set`两个主要的静态方法：

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

成员名和类型可以在路径上混用，但暂不支持在同一级路径既指定成员名称又指定类型的写法

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
> `OmniAccessor`具有基于Fail-Fast机制的防代码重构能力，当用户提供的访问路径无法匹配到任何成员时，`OmniAccessor`将立即抛出异常，使单元测试提前终止。然而相比常规的成员访问方式，`OmniAccessor`在IDE重构方面的支持依然偏弱。
>
> 对于复杂对象的内容赋值，大多数情况下，我们更推荐使用[构造者模式](https://developer.aliyun.com/article/705058)，或者暴露Getter/Setter方法实现。这些常规手段虽然稍显笨拙（尤其在需要为许多相似的成员批量赋值的时候），但对业务逻辑的封装和重构都更加友好。
> 仅当原类型不适合改造，且没有其它可访问目标成员的方法时，`OmniAccessor`才是最后的终极工具。
>
> 出于相同的原因，虽然技术上可行，但我们并不推荐在除单元测试之外的场景使用`OmniAccessor`方式来读写业务类的成员字段。
