主流Mock工具对比
---

除`TestableMock`外，目前主要的Mock工具主要有`Mockito`、`Spock`、`PowerMock`和`JMockit`，基本差异如下：

|  工具         | 原理           | 最小Mock单元  | 对被Mock方法的限制          | 上手难度   | IDE支持  |
|  ----        | ----          | ----         | ----                     | ----      | ----    |
| Mockito      | 动态代理        | 类           | 不能Mock私有/静态和构造方法  | **较容易** | **很好** |
| Spock        | 动态代理        | 类           | 不能Mock私有/静态和构造方法  | 较复杂     | 一般     |
| PowerMock    | 自定义类加载器   | 类           | **任何方法皆可**           | 较复杂     | **较好** |
| JMockit      | 运行时字节码修改 | 类            | 不能Mock构造方法(new操作符) | 较复杂     | 一般     |
| TestableMock | 运行时字节码修改 | 方法          | **任何方法皆可**           | **很容易** | 一般     |

`Mockito`是Java最老牌的Mock工具，稳定性和易用性较好，IntelliJ和Eclipse都有专用插件支持。相对不足之处在于Mock功能稍弱，在必要情况下需与其他Mock工具配合使用。

`Spock`是一款代码可读性非常高的单元测试框架，内置Mock支持，具有很好的整体感。由于同样基于动态代理实现，其不足点与`Mockito`类似。

`PowerMock`是一款功能十分强大的Mock工具，其基本语法与`Mockito`兼容，同时扩展了许多`Mockito`缺失的功能，包括对支持对私有、静态和构造方法实施Mock。但由于使用了自定义类加载器，会导致Jacoco在默认的`on-the-fly`模式下覆盖率跌零。

`JMockit`是一款功能性与易用性均居于`Mockito`与`PowerMock`之间的Mock工具，较好的弥补了两者各自的不足。该项目在2017年尝试推出JMockit2重写版本但未能完成，目前处于不活跃的维护状态。

相比之下，`TestabledMock`的功能与`PowerMock`基本平齐，且极易上手，只需掌握`@MockMethod`注解就可以完成绝大多数任务。

当前`TestableMock`的主要不足在于，编写Mock方法时IDE无法即时提示方法参数是否正确匹配。若发现匹配效果不符合预期，需要通过[自助问题排查](zh-cn/doc/troubleshooting.md)文档提供的方法在运行期进行校验。这个功能理论上能够通过扩展主流IDE插件来补充，但目前暂无相关开发计划，参见[Issue-104](https://github.com/alibaba/testable-mock/issues/104)。

此外，由于`TestableMock`独辟蹊径的采用“每个业务类拥有一个专属Mock容器类”的思维方式，将Mock方法定义与单元测试用例解耦，一方面使得Mock方法具有默认可复用性，单元测试用例也因此变得更干净纯粹，另一方面也导致Mock方法定义变得零散，生命周期管理起来相对困难，对现有开发者的Mock编写习惯会带来一定改变。
