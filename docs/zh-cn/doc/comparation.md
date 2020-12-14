主流Mock工具对比
---

除`TestableMock`外，目前主要的Mock工具主要有`Mockito`、`PowerMock`和`JMockit`，基本差异如下：

|  工具         | 原理           | 最小Mock单元  | 对被Mock方法的限制          | 上手难度   | IDE支持  |
|  ----        | ----          | ----         | ----                     | ----      | ----    |
| Mockito      | 动态代理        | 类           | 不能Mock私有/静态和构造方法  | **较容易** | **很好** |
| PowerMock    | 自定义类加载器   | 类           | **任何方法皆可**           | 较繁琐     | **较好** |
| JMockit      | 运行时字节码修改 | 类            | 不能Mock构造方法(new操作符) | 较繁琐     | 一般     |
| TestableMock | 运行时字节码修改 | 方法          | **任何方法皆可**           | **很容易** | 一般     |

相比之下，`TestabledMock`的功能与`PowerMock`基本平齐，且极易上手，只需掌握`@MockMethod`注解就可以完成绝大多数任务。

当前`TestableMock`的主要不足在于，编写Mock方法时IDE尚无法即时提示方法参数是否正确匹配。若发现匹配效果不符合预期，需要通过[自助问题排查](zh-cn/doc/troubleshooting.md)文档提供的方法在运行期进行校验。这个功能未来需要通过扩展主流IDE插件来提供。
