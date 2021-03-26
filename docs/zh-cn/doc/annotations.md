注解参数清单
---

基于轻量的原则，`TestableMock`为开发者提供了尽可能精炼、易用的注解组合，以下参数信息可供开发参考。

#### @EnablePrivateAccess

启用对被测类的<u>私有成员访问编译期增强</u>和<u>私有目标存在性的编译期校验</u>功能。

- 作用于：测试类

| 参数                   | 类型    | 是否必须 | 默认值 | 作用 |
| ---                   | ---     | ---     | ---- | ---  |
| srcClass              | Class   | 否      | N/A   | 当测试类命名不符合约定时，指定实际被测类 |
| verifyTargetOnCompile | boolean | 否      | true  | 是否启用私有目标的编译期存在性校验 |

#### @MockMethod

将当前方法标识为待匹配的Mock成员方法。

- 作用于：Mock容器类中的方法

| 参数          | 类型       | 是否必须 | 默认值           | 作用 |
| ---          | ---       | ---     | ----             | ---  |
| targetClass  | Class     | 否      | N/A              | 指定Mock目标的调用者类型 |
| targetMethod | String    | 否      | N/A              | 指定Mock目标的方法名 |
| scope        | MockScope | 否      | MockScope.GLOBAL | 指定Mock的生效范围 |

#### @MockConstructor

将当前方法标识为待匹配的Mock构造方法。

- 作用于：Mock容器类中的方法

| 参数   | 类型      | 是否必须 | 默认值            | 作用 |
| ---   | ---       | ---    | ----             | ---  |
| scope | MockScope | 否      | MockScope.GLOBAL | 指定Mock的生效范围 |


#### @MockWith

显式指定当前类型关联的Mock容器类。

- 作用于：测试类、被测类

| 参数（`N/A`为默认参数） | 类型       | 是否必须 | 默认值                 | 作用 |
| ---                  | ---       | ---     | ----                  | ---  |
| N/A                  | Class     | 否      | NullType.class        | 指定使用的Mock容器类 |
| treatAs              | ClassType | 否      | ClassType.GuessByName | 指定当前类是测试类或被测类 | 

#### @MockDiagnose

启用或禁止Mock相关的诊断信息输出。

- 作用于：Mock容器类

| 参数（`N/A`为默认参数） | 类型      | 是否必须 | 默认值 | 作用 |
| ---                  | ---      | ---     | ----  | ---  |
| N/A                  | LogLevel | 是      | N/A   | 指定当前Mock容器关联测试用例的诊断日志级别 |

