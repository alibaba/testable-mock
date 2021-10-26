Self-Help Troubleshooting
---

Compared with `Mockito` and other mock tools where developers have to manually inject mock classes, `TestableMock` uses method name and parameter type matching to automatically find invocations that require mock. While this mechanism brings convenience, it may also cause unexpected mock replacement.

For this reason, `TestableMock` will automatically save the mock scanning log of the last test run in the project build directory. The default location is `target/testable-agent.log` (Maven project) or `build/testable-agent.log` (Gradle project).

Examples of log content are as follows:

```text
[INFO] Start at Mon Jan 00 00:00:00 CST 0000
... ...
[INFO] Found test class com/alibaba/testable/demo/basic/DemoMockTest
[INFO]   Found 6 test cases
[INFO] Found mock class com/alibaba/testable/demo/basic/DemoMockTest$Mock
[INFO]   Found 8 mock methods
[INFO] Found source class com/alibaba/testable/demo/basic/DemoMock
[INFO]   Found method <init>
[INFO]   Found method newFunc
[INFO]     Line 19, mock method "createBlackBox" used
[INFO]   Found method outerFunc
[INFO]     Line 27, mock method "innerFunc" used
[INFO]     Line 27, mock method "staticFunc" used
[INFO]   Found method commonFunc
[INFO]     Line 34, mock method "trim" used
[INFO]     Line 34, mock method "sub" used
[INFO]     Line 34, mock method "startsWith" used
... ...
[INFO] Completed at Mon Jan 00 00:00:00 CST 0000
```

The log shows all the mocked invocation and corresponding code line numbers in the class under test.

According to the targeted test classes, below are some simple clues for self-troubleshooting. Suppose the class under test is "com.demo.BizService", the test class is "com.demo.BizServiceTest", and the mock container class is "com.demo.BizServiceTest.Mock":

- If the log file is no generated, please check whether the `pom.xml` or `build.gradle` configuration correctly introduces `TestableMock` dependencies
- If only `com/demo/BizServiceTest$Mock` is found in the output, please check whether the mock class is created at correct place
- If both the test class and the mock class are found, but the class under test `com/demo/BizService` not appeared, please check whether the test class is in the same package of the class under test, and the name is "<ClassUnderTest>+Test", otherwise `@MockWith` annotation should be used
- If all the three classes are found, and `Found method xxx` are output, but there is no mock replacement happen at the expected code line, please check whether the mock method definition matches the target method

For situations where expected mocking is not take effect, you could add a `@MockDiagnose` annotation to the mock class, and set the diagnosis level to `LogLevel.VERBOSE` for further investigation information.

```java
class BizServiceTest {
    @MockDiagnose(LogLevel.VERBOSE)
    public static class Mock {
        ...
    }
}
```

Executing the unit test again will print out the signatures of all mock methods, and the signatures of all invocations scanned in the class under test:

```text
[INFO] Found test class com/alibaba/testable/demo/basic/DemoMockTest
[TIP]    Test case "should_mock_new_object"
... ...
[TIP]    Test case "should_set_mock_context"
[INFO]   Found 6 test cases
[INFO] Found mock class com/alibaba/testable/demo/basic/DemoMockTest$Mock
[TIP]    Mock constructor "createBlackBox" as "com.alibaba.demo.basic.model.mock.BlackBox(java.lang.String)"
[TIP]    Mock method "innerFunc" as "com.alibaba.demo.basic.DemoMock::innerFunc(java.lang.String) : java.lang.String"
... ...
[TIP]    Mock method "callFromDifferentMethod" as "()Ljava/lang/String;"
[INFO]   Found 8 mock methods
[INFO] Found source class com/alibaba/testable/demo/basic/DemoMock
[INFO]   Found method <init>
[TIP]      Line 13, constructing "java.lang.Object()"
[INFO]   Found method newFunc
[TIP]      Line 19, constructing "com.alibaba.demo.basic.model.mock.BlackBox(java.lang.String)"
[INFO]     Line 19, mock method "createBlackBox" used
[TIP]      Line 19, invoking "com.alibaba.demo.basic.DemoMockTest$Mock::createBlackBox(java.lang.String) : com.alibaba.demo.basic.model.mock.BlackBox"
[TIP]      Line 20, invoking "com.alibaba.demo.basic.model.mock.BlackBox::get() : java.lang.String"
[INFO]   Found method outerFunc
[TIP]      Line 27, constructing "java.lang.StringBuilder()"
[TIP]      Line 27, invoking "java.lang.StringBuilder::append(java.lang.String) : java.lang.StringBuilder"
[TIP]      Line 27, invoking "com.alibaba.demo.basic.DemoMock::innerFunc(java.lang.String) : java.lang.String"
[INFO]     Line 27, mock method "innerFunc" used
... ...
```

The logs are formatted in follow pattern:

- `Mock constructor "<MockMethodName>" as "<Signature>" for "<TypeName>"` Mock constructor found in test class
- `Mock method "<MockMethodName>" as "<Signature>"` Mock method found in test class (the first parameter that identify the mock target class is currently kept)
- `Line XX, constructing "<TypeName>" as "<Signature>"` Constructor invocation found in test under class
- `Line XX, invoking "<MethodName>" as "<Signature>"` Member method invocation found in test under class

> In order to clearly distinguish between the type of method return value and the type of invoker, the method signature recorded in the log uses a method definition structure similar to `Kotlin`.

Comparing the actual signature of the original call with the signature defined by the mock method, the problem is usually found quickly.
