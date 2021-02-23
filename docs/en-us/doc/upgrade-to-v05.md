Upgrade To Version 0.5
---

After nearly a month of design and development, the `0.5` version of TestableMock has finally come out. Compared with the `0.4` version, the new version solves the three historical problems left over before:

1. <s>**Mock method cannot call other non-static methods**</s>. The mock method in the new version no longer has any difference from the ordinary method, and can access any external method and member variable.
2. <s>**Mock method always acts on the entire test life cycle**</s>. From now on, the mock method supports restricting the effective scope to the test cases in the test class of which it belongs, so there is no need to worry about accidentally mocking cross-class test invocations.
3. <s>**The MOCK_CONTEXT needs manually cleaned up and only supports class-level parallel testing**</s>. Now each test case has an independent `MOCK_CONTEXT` variable, no need to clean up after used, and you can use unit test with any parallel level.

In order to better realize the reuse of Mock methods, we have made a clear boundary between the mock class and the test class in the new version. When upgrading from `0.4` to `0.5`, the only change required is to wrap all mock methods in the test class with a `public static class Mock {}`.

For example, the original test class definition was as follows:

```java
public class DemoMockTest {
    @MockMethod(targetClass = DemoMock.class)
    private String innerFunc(String text) {
        return "hello_" + text;
    }
        
    @Test
    void should_able_to_mock_member_method() throws Exception {
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
    void should_able_to_mock_member_method() throws Exception {
        assertEquals("hello_world", demoMock.outerFunc());
        verify("innerFunc").with("world");
    }
}
```

Then upgrade the `TestableMock` dependency in the `pom.xml` or `build.gradle` file to `0.5.0` or above.
