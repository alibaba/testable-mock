Mock Tools Comparison
---

Besides `TestableMock`, there are also several other community mock tools, such as `Mockito`, `Spock`, `PowerMock` and `JMockit`. Comparison as follows:

|  Tool        | Mechanism                     | Minimal mock unit | Limitation of method be mocked               | Ease of use   | IDE support   |
|  ----        | ----                          | ----              | ----                                         | ----          | ----          |
| Mockito      | Dynamic proxy                 | Class             | Except private/static method and constructor | **Easy**      | **Very well** |
| Spock        | Dynamic proxy                 | Class             | Except private/static method and constructor | Complicate    | Just so so    |
| PowerMock    | Custom class loader           | Class             | **No limitation, any method works**          | Complicate    | **Good**      |
| JMockit      | Runtime bytecode modification | Class             | Except constructor (i.e. new operator)       | Complicate    | Just so so    |
| TestableMock | Runtime bytecode modification | Method            | **No limitation, any method works**          | **Very easy** | Just so so    |

`Mockito` is Java's the most classical mock tool, good stability and easy to use, with both IntelliJ and Eclipse have plugin. The shortcoming part is that its mock functionality is sometimes not enough and have to be used in conjunction with other mock tools when necessary.

`Spock` is a highly readable unit testing framework with built-in mock support, it has a good overall consistency. Since it is also based on dynamic proxy implementation, its shortcomings are similar to `Mockito`.

`PowerMock` is a very powerful mock tool. Its basic syntax is compatible with `Mockito`, and extends many missing features of `Mockito`, including support for mocking of private, static and construction methods. However, due to the use of custom class loader, the test coverage of Jacoco will drop to zero in the default `on-the-fly` mode.

`JMockit` is a mock tool whose functionality and convenience are between `Mockito` and `PowerMock`, and it makes up for their respective shortcomings. The project tried to launch a rewritten version of JMockit2 in 2017 but failed to complete, and is currently in an inactive maintenance state.

The functionality of `TestabledMock` is basically the same as that of `PowerMock`, and it is extremely easy to use. You can complete most tasks only by mastering the annotations of `@MockMethod`.

The main disadvantage of the current `TestableMock` is that the IDE cannot promptly prompt whether the method parameters are matched correctly when writing the mock method. If the mocking effect does not meet expectation, it has to be verified during runtime through the method provided in the [self-help troubleshooting](en-us/doc/troubleshooting.md) document. This feature needs to be provided by extending IDE plugins in the future.

In addition, because `TestableMock` uses the mechanism based on "each source class binding to its own mock class" in a unique way, it decouples the mock method definition from the unit test cases. Thus, the mock methods are by default reusable, and the unit test cases become cleaner and purer. On the other hand, the mock methods become fragmented, and life cycle management is relatively difficult. Therefore, it may take some time for developers get used to it.
