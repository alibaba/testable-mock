Fast Mocking
---

In unit testing, the main role of the mock method is to replace those methods with **need external dependencies**, **with time-consuming**, **has random results**, or other effects that affect the development of the test, but do not affect the key the logic to be tested. Generally speaking, a certain call needs to be mocked, which is usually only related to its own characteristics, and has nothing to do with the source of the invocation.

Based on the above information, `TestableMock` has designed a minimalist mock mechanism. Unlike the common mock tools that uses **class** as the definition granularity of mocking, and repeats the description of mock behavior in each test case, `TestableMock` allows each business class (class under test) to be associated with a set of reusable collection of mock methods (carried by the mock container class), following the principle of "convention over configuration", and mock method replacement will automatically happen when the specified method in the test class match an invocation in the class under test.

> In summary, there are two simple rules:
> - Mock non-constructive method, copy the original method definition to the mock class, add a `@MockMethod` annotation
> - Mock construction method, copy the original method definition to the mock class, replace the return value with the constructed type, the method name is arbitrary, and add a `@MockContructor` annotation

The detail mock method definition convention is as follows.

### 0. Pre-step, prepare the mock class

First, create a mock class as the container for mock methods associated with the test class. The simplest way is to add a static inner class named `Mock` to the test class. E.g:

```java
public class DemoTest {

    public static class Mock {
        // mock methods goes here
    }

}
```

### 1.1 Mock method calls of any class

Define an ordinary method annotated with `@MockMethod` in the mock class with exactly the same signature (name, parameter, and return value type) as the method to be mocked, and then add the type of target object (which the method originally belongs to) as `targetMethod` parameter of `@MockMethod` annotation.

At this time, all invocations to that original method in the class under test will be automatically replaced with invocations to the above-mentioned mock method when the unit test is running.

For example, there is a call to `"anything".substring(1, 2)` in the class under test, and we want to change it to a fixed string when running the test, we only need to define the following method in the mock class:

```java
// The original method signature is `String substring(int, int)`
// The object `"anything"` that invokes this method is of type `String`
@MockMethod(targetClass = String.class)
private String substring(int i, int j) {
    return "sub_string";
}
```

When several methods to be mocked have the same name, you can put the name of the method to be mocked in the `targetMethod` parameter of `@MockMethod` annotation, so that the mock method itself can be named at will.

The following example shows the usage of the `targetMethod` parameter, and its effect is the same as the above example:

```java
// Use `targetMethod` to specify the name of the method that needs to be mocked
// The method itself can now be named arbitrarily, but the method parameters still need to follow the same matching rules
@MockMethod(targetClass = String.class, targetMethod = "substring")
private String use_any_mock_method_name(int i, int j) {
    return "sub_string";
}
```

Sometimes, the mock method need to access the member variables in the original object that initiated the invocation, or invoke other methods of the original object. At this point, you can remove the `targetClass` parameter in the `@MockMethod` annotation, and then add a extra parameter whose type is the original object type of the method to the first index of the method parameter list.

The `TestableMock` convention is that when the `targetClass` parameter of the `@MockMethod` annotation is not defined, the first parameter of the mock method is the type of the target method, and the parameter name is arbitrary. In order to facilitate code reading, it is recommended to name this parameter as `self` or `src`. Example as follows:

```java
// Adds a `String` type parameter to the first position the mock method parameter list (parameter name is arbitrary)
// This parameter can be used to get the value and context of the actual invoker at runtime
@MockMethod
private String substring(String self, int i, int j) {
    // Call the original method is also allowed
    return self.substring(i, j);
}
```

For complete code examples, see the `should_mock_common_method()` test cases in the `java-demo` and `kotlin-demo` sample projects. (Because `Kotlin` has made magical changes to the String type, the method under test in the `Kotlin` example adds a layer of encapsulation to the `BlackBox` class)

### 1.2 Mock the member method of the class under test itself

Sometimes, when testing certain methods, it is desirable to mock out some other member methods of the class under test itself.

The solution is the same as the previous case. Just set `targetClass` parameter value to the type of class under test.

For example, there is a private method with the signature `String innerFunc(String)` in the class under test. If we want to replace it during testing, we only need to define the following method in the mock class:

```java
// The type to test is `DemoMock`
@MockMethod(targetClass = DemoMock.class)
private String innerFunc(String text) {
    return "mock_" + text;
}
```

Similarly, if the method in the above example needs to access the original tested object that initiated the call, it may not use the `targetClass` parameter, but when defining the mock method, add a parameter of type `DemoMock` to the first index of the method parameter list.

For complete code examples, see the `should_mock_member_method()` test case in the `java-demo` and `kotlin-demo` sample projects.

### 1.3 Mock static methods of any class

Mock for static methods is the same as for any ordinary methods.

For example, if the static method `secretBox()` of the `BlackBox` type is invoked in the class under test, and the method signature is `BlackBox secretBox()`, then the mock method is as follows:

```java
@MockMethod(targetClass = BlackBox.class)
private BlackBox secretBox() {
    return new BlackBox("not_secret_box");
}
```

For complete code examples, see the `should_mock_static_method()` test case in the `java-demo` and `kotlin-demo` sample projects.

### 1.4 Mock `new` operation of any type

Define an ordinary method annotated with `@MockContructor` in the mock class, make the return value type of the method the type of the object to be created, and the method parameters are exactly the same as the constructor parameters to be mocked, the method name is arbitrary.

At this time, all operations in the class under test that use `new` to create the specified class (and use the constructor that is consistent with the mock method parameters) will be replaced with calls to the custom method.

For example, if there is a call to `new BlackBox("something")` in the class under test, and you want to change it during unit testing (usually by replacing it with a mock object, or by replacing it with a temporary object created with test parameters), just define the following mock method:

```java
// The signature of the constructor to be mocked is `BlackBox(String)`
// No need to add additional parameters to the mock method parameter list, and the name of the mock method is arbitrary 
@MockContructor
private BlackBox createBlackBox(String text) {
    return new BlackBox("mock_" + text);
}
```

> You can still use the `@MockMethod` annotation, and configure the `targetMethod` parameter value to `"<init>"`, and the rest is the same as above. The effect is the same as using the `@MockContructor` annotation

For complete code examples, see the `should_mock_new_object()` test case in the `java-demo` and `kotlin-demo` sample projects.

### 2. Identify different invocation source in mock method

In the mock method, you can use the `TestableTool.SOURCE_METHOD` variable to identify **the method name of the class under test before entering the mock method**; in addition, the `TestableTool.MOCK_CONTEXT` variable can **inject additional context parameters into the mock method**, to distinguish and process different calling scenarios.

For example, to verify the impact on the target method under test when the mock method returns different results in the test case:

```java
@Test
public void testDemo() {
    MOCK_CONTEXT.set("case", "data-ready");
    assertEquals(true, demo());
    MOCK_CONTEXT.set("case", "has-error");
    assertEquals(false, demo());
}
```

Take out the injected parameters in the mock method and return different results according to the situation:

```java
@MockMethod
private Data mockDemo() {
    switch((String)MOCK_CONTEXT.get("case")) {
        case "data-ready":
            return new Data();
        case "has-error":
            throw new NetworkException();
        default:
            return null;
    }
}
```

For complete code examples, see the `should_get_source_method_name()` and `should_get_test_case_name()` test cases in the `java-demo` and `kotlin-demo` sample projects.

### 3. Verify the sequence and parameters of the mock method being invoked

In test cases, you can use the `InvokeVerifier.verify()` method, and cooperate with `with()`, `withInOrder()`, `without()`, `withTimes()` and other methods to verify the mock call situation.

For details, please refer to the [Check Mock Call](en-us/doc/matcher.md) document.

### 4. Special instructions

> **Naming Conventions for Test Classes and Mock Containers**
>
> By default, `TestableMock` assumes that the <u>package path of the test class and the class under test are the same, and the name is `<ClassUnderTest>+Test`</u> (usually Java project with `Maven` or `Gradle` conform to this convention).
> At the same time, it is agreed that the mock container associated with the test class is <u>in its internal static class named `Mock`</u>, or <u>an independent class named `<ClassUnderTest>+Mock` under the same package path</u>.
>
> When the test class or mock container path does not follow to this convention, you can use the `@MockWith` annotation to specify it explicitly. For details, see [Use MockWith Annotation](en-us/doc/use-mock-with.md).

For more implementation details of `TestableMock`, please refer to the [Design and Principle](en-us/doc/design-and-mechanism.md) document.
