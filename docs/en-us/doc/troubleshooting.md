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
[DIAGNOSE] Handling test class com/alibaba/testable/demo/DemoMockTest
[DIAGNOSE] Handling source class com/alibaba/testable/demo/DemoMock
[DIAGNOSE]   Found 7 mock methods
[DIAGNOSE]   Handling method <init>
[DIAGNOSE]   Handling method newFunc
[DIAGNOSE]     Line 14, mock method createBlackBox used
[DIAGNOSE]   Handling method outerFunc
[DIAGNOSE]     Line 22, mock method innerFunc used
[DIAGNOSE]   Handling method commonFunc
[DIAGNOSE]     Line 29, mock method trim used
[DIAGNOSE]     Line 29, mock method sub used
[DIAGNOSE]     Line 29, mock method startsWith used
[DIAGNOSE]   Handling method getBox
[DIAGNOSE]     Line 36, mock method secretBox used
[DIAGNOSE]   Handling method callerOne
[DIAGNOSE]     Line 43, mock method callFromDifferentMethod used
[DIAGNOSE]   Handling method callerTwo
[DIAGNOSE]     Line 47, mock method callFromDifferentMethod used
[DIAGNOSE]   Handling method innerFunc
[DIAGNOSE]   Handling method callFromDifferentMethod
```

The log shows all the mocked invocation and corresponding code line numbers in the class under test.

- Self troubleshooting:

- If there is no output, please check whether the `pom.xml` or `build.gradle` configuration correctly introduces `TestableMock` dependencies
- If only the first line of `Handling test class` is output, please check whether the test class is in the same package of the class under test, and the name is "<ClassUnderTest>+Test" (required for `0.4.x` version)
- If `Handling source class` and `Handling method xxx` are output, but there is no mock replacement happen at the expected code line, please check whether the mock method definition matches the target method
