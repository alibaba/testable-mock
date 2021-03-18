Self-Help Troubleshooting
---

Compared with `Mockito` and other mock tools where developers have to manually inject mock classes, `TestableMock` uses method name and parameter type matching to automatically find invocations that require mock. While this mechanism brings convenience, it may also cause unexpected mock replacement.

To troubleshoot mock-related issues, just add the `@MockWith` annotation to the test class, and configure the parameter `diagnose` to `MockDiagnose.ENABLE`, so the detailed mock method replacement process will be printed when the test is run.

```java
@MockWith(diagnose = MockDiagnose.ENABLE)
class DemoTest {
    ...
}
```

The output log example is as follows:

```text
[DIAGNOSE] Handling test class com/alibaba/testable/demo/basic/DemoMockTest
[DIAGNOSE]   Found 6 test cases
[DIAGNOSE] Handling mock class com/alibaba/testable/demo/basic/DemoMockTest$Mock
[DIAGNOSE]   Found 8 mock methods
[DIAGNOSE] Handling source class com/alibaba/testable/demo/basic/DemoMock
[DIAGNOSE]   Handling method <init>
[DIAGNOSE]   Handling method newFunc
[DIAGNOSE]     Line 19, mock method "createBlackBox" used
[DIAGNOSE]   Handling method outerFunc
[DIAGNOSE]     Line 27, mock method "innerFunc" used
[DIAGNOSE]     Line 27, mock method "staticFunc" used
[DIAGNOSE]   Handling method commonFunc
[DIAGNOSE]     Line 34, mock method "trim" used
[DIAGNOSE]     Line 34, mock method "sub" used
[DIAGNOSE]     Line 34, mock method "startsWith" used
... ...
```

The log shows all the mocked invocation and corresponding code line numbers in the class under test.

- Self troubleshooting:

- If there is no output, please check whether the `pom.xml` or `build.gradle` configuration correctly introduces `TestableMock` dependencies
- If only the first line of `Handling test class` is output, please check whether the test class is in the same package of the class under test, and the name is "<ClassUnderTest>+Test" (required for `0.4.x` version)
- If `Handling source class` and `Handling method xxx` are output, but there is no mock replacement happen at the expected code line, please check whether the mock method definition matches the target method

For situations where expected mocking is not take effect, you could set the diagnosis level to `MockDiagnose.VERBOSE` for further investigation information.

```java
@MockWith(diagnose = MockDiagnose.VERBOSE)
class DemoTest {
    ...
}
```

Executing the unit test again will print out the signatures of all mock methods, and the signatures of all invocations scanned in the class under test:

```text
[DIAGNOSE] Handling test class com/alibaba/testable/demo/basic/DemoMockTest
[VERBOSE]    Test case "should_mock_new_object"
... ...
[VERBOSE]    Test case "should_set_mock_context"
[DIAGNOSE]   Found 6 test cases
[DIAGNOSE] Handling mock class com/alibaba/testable/demo/basic/DemoMockTest$Mock
[VERBOSE]    Mock constructor "createBlackBox" as "com.alibaba.demo.basic.model.BlackBox(java.lang.String)"
[VERBOSE]    Mock method "innerFunc" as "com.alibaba.demo.basic.DemoMock::innerFunc(java.lang.String) : java.lang.String"
... ...
[VERBOSE]    Mock method "callFromDifferentMethod" as "()Ljava/lang/String;"
[DIAGNOSE]   Found 8 mock methods
[DIAGNOSE] Handling source class com/alibaba/testable/demo/basic/DemoMock
[DIAGNOSE]   Handling method <init>
[VERBOSE]      Line 13, constructing "java.lang.Object()"
[DIAGNOSE]   Handling method newFunc
[VERBOSE]      Line 19, constructing "com.alibaba.demo.basic.model.BlackBox(java.lang.String)"
[DIAGNOSE]     Line 19, mock method "createBlackBox" used
[VERBOSE]      Line 19, invoking "com.alibaba.demo.basic.DemoMockTest$Mock::createBlackBox(java.lang.String) : com.alibaba.demo.basic.model.BlackBox"
[VERBOSE]      Line 20, invoking "com.alibaba.demo.basic.model.BlackBox::get() : java.lang.String"
[DIAGNOSE]   Handling method outerFunc
[VERBOSE]      Line 27, constructing "java.lang.StringBuilder()"
[VERBOSE]      Line 27, invoking "java.lang.StringBuilder::append(java.lang.String) : java.lang.StringBuilder"
[VERBOSE]      Line 27, invoking "com.alibaba.demo.basic.DemoMock::innerFunc(java.lang.String) : java.lang.String"
[DIAGNOSE]     Line 27, mock method "innerFunc" used
... ...
```

The logs are formatted in follow pattern:

- `Mock constructor "<MockMethodName>" as "<Signature>" for "<TypeName>"` Mock constructor found in test class
- `Mock method "<MockMethodName>" as "<Signature>"` Mock method found in test class (the first parameter that identify the mock target class is currently kept)
- `Line XX, constructing "<TypeName>" as "<Signature>"` Constructor invocation found in test under class
- `Line XX, invoking "<MethodName>" as "<Signature>"` Member method invocation found in test under class
