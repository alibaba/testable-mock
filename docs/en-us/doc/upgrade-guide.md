Upgrade Guide
---

### Upgrade to v0.6

In version `0.6`，`TestableMock` provided a new [quick complicated class construction](en-us/doc/omni-constructor.md) capability. Meanwhile, it includes a modification that is incompatible with the `0.5` version: class `PrivateAccessor` have been moved from `com.alibaba.testable.core.accessor` package to `com.alibaba.testable.core.tool` package.

If you are using `PrivateAccessor` and having import statement like this:

```java
import com.alibaba.testable.core.accessor.PrivateAccessor;
```

When upgrade `TestableMock` to `0.6` version, please replace it as：

```java
import com.alibaba.testable.core.tool.PrivateAccessor;
```

### Upgrade to v0.5

The `0.5` version solves the three historical problems left over before:

1. <s>**Mock method cannot call other non-static methods**</s>. The mock method in the new version no longer has any difference from the ordinary method, and can access any external method and member variable.
2. <s>**Mock method always acts on the entire test life cycle**</s>. From now on, the mock method supports restricting the effective scope to the test cases in the test class of which it belongs, so there is no need to worry about accidentally mocking cross-class test invocations.
3. <s>**The MOCK_CONTEXT needs manually cleaned up and only supports class-level parallel testing**</s>. Now each test case has an independent `MOCK_CONTEXT` variable, no need to clean up after used, and you can use unit test with any parallel level.

In order to better realize the reuse of Mock methods, the version `0.5` have made a clear boundary between the mock class and the test class in the new version. When upgrading from `0.4` to `0.5`, the only change required is to wrap all mock methods in the test class with a `public static class Mock {}`.

For example, the original test class definition was as follows:

```java
public class DemoMockTest {
    @MockMethod(targetClass = DemoMock.class)
    private String innerFunc(String text) {
        return "hello_" + text;
    }
        
    @Test
    void should_mock_member_method() throws Exception {
        assertEquals("hello_world", demoMock.outerFunc());
        verify("innerFunc").with("world");
    }
}
```

After upgrading to the `0.5` version, move all mock methods (in this example, only the `innerFunc` method) to a static inner class named `Mock`, which is equivalent to adding two lines of code:

```java
public class DemoMockTest {

    public static class Mock {   // Add this line
        @MockMethod(targetClass = DemoMock.class)
        private String innerFunc(String text) {
            return "hello_" + text;
        }
    }                            // Add this line
        
    @Test
    void should_mock_member_method() throws Exception {
        assertEquals("hello_world", demoMock.outerFunc());
        verify("innerFunc").with("world");
    }
}
```

Finally, upgrade the `TestableMock` dependency in `pom.xml` or `build.gradle` file to the new version.
