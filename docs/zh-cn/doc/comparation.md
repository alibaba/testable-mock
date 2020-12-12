主流Mock工具对比
---

除`TestableMock`外，目前主要的Mock工具主要有`Mockito`、`PowerMock`和`JMockit`，基本差异如下：

|  工具         | 原理           | 最小Mock单元  | 对被Mock方法的限制          | 上手难度 |
|  ----        | ----          | ----         | ----                     | ----    |
| Mockito      | 动态代理        | 类           | 不能Mock私有/静态和构造方法  | **低**  |
| PowerMock    | 自定义类加载器   | 类           | **任何方法皆可**           | 高      |
| JMockit      | 运行时字节码修改 | 类            | 不能Mock构造方法(new操作符) | 高      |
| TestableMock | 运行时字节码修改 | 方法          | **任何方法皆可**           | **低**  |

