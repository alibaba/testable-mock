Fast Mocking
---

Compared with the class-granularity mocking practices of existing mock tools, `TestableMock` allows developers to directly define a single method and use it for mocking. With the principle of convention over configuration, mock method replacement will automatically happen when the specified method in the test class match an invocation in the class under test.

> In summary, there are two simple rules:
> - Mock non-constructive method, copy the original method definition to the test class, add a parameter of the same type as the caller, and add a `@MockMethod` annotation
> - Mock construction method, copy the original method definition to the test class, replace the return value with the constructed type, the method name is arbitrary, and add a `@MockContructor` annotation

> **Note**: There is also a convention in the current version that the name of the test class should be `<NameOfClassUnderTest> + Test`, which is usually the by-default naming convention of Java unit tests. This constraint may be relaxed or removed in future versions of `TestableMock`.

The detail mock method definition convention is as follows:

#### 1. Mock method calls of any class

Define an ordinary method annotated with `@MockMethod` in the test class with exactly the same signature (name, parameter, and return value type) as the method to be mocked, and then add an extra parameter as the first parameter of method, with the same type as the object that the method originally belongs to.

At this time, all invocations to that original method in the class under test will be automatically replaced with invocations to the above-mentioned mock method when the unit test is running.

**Note**: When several methods to be mocked have the same name, you can put the name of the method to be mocked in the `targetMethod` parameter of `@MockMethod` annotation, so that the mock method itself can be named at will.

For example, there is a call to `"anything".substring(1, 2)` in the class under test, and we want to change it to a fixed string when running the test, we only need to define the following method in the test class:

```java
// The original method signature is `String substring(int, int)`
// The object `"anything"` that invokes this method is of type `String`
// Adds a `String` type parameter to the first position the mock method parameter list (parameter name is arbitrary)
// This parameter can be used to get the value and context of the actual invoker at runtime
@MockMethod
private String substring(String self, int i, int j) {
    return "sub_string";
}
```

The following example shows the usage of the `targetMethod` parameter, and its effect is the same as the above example:

```java
// Use `targetMethod` to specify the name of the method that needs to be mocked
// The method itself can now be named arbitrarily, but the method parameters still need to follow the same matching rules
@MockMethod(targetMethod = "substring")
private String use_any_mock_method_name(String self, int i, int j) {
    return "sub_string";
}
```

For complete code examples, see the `should_able_to_mock_common_method()` test cases in the `java-demo` and `kotlin-demo` sample projects. (Because Kotlin has made magical changes to the String type, the method under test in the Kotlin example adds a layer of encapsulation to the `BlackBox` class)

#### 2. Mock the member method of the class under test itself

Sometimes, when testing certain methods, it is desirable to mock out some other member methods of the class under test itself.

The solution is the same as the previous case. The first parameter type of the mock method needs to be the same as that of the class under test.

For example, there is a private method with the signature `String innerFunc(String)` in the class under test. If we want to replace it during testing, we only need to define the following method in the test class:

```java
// The type to test is `DemoMock`
// So when defining the mock method, add a parameter of type `DemoMock` to the first position of parameter list (the name is arbitrary)
@MockMethod
private String innerFunc(DemoMock self, String text) {
    return "mock_" + text;
}
```

For complete code examples, see the `should_able_to_mock_member_method()` test case in the `java-demo` and `kotlin-demo` sample projects.

#### 3. Mock static methods of any class

Mock for static methods is the same as for any ordinary methods. But it should be noted that when the mock method of a static method is called, the actual value of the first parameter passed in is always `null`.

For example, if the static method `secretBox()` of the `BlackBox` type is invoked in the class under test, and the method signature is changed to `BlackBox secretBox()`, the mock method is as follows:

```java
// The target static method is defined in the `BlackBox` type
// When defining the mock method, add a parameter of type `BlackBox` to the first position parameter list (the name is arbitrary)
// This parameter is only used to identify the target type, the actual incoming value will always be `null`
@MockMethod
private BlackBox secretBox(BlackBox ignore) {
    return new BlackBox("not_secret_box");
}
```

For complete code examples, see the `should_able_to_mock_static_method()` test case in the `java-demo` and `kotlin-demo` sample projects.

#### 4. Mock `new` operation of any type

Define an ordinary method annotated with `@MockContructor` in the test class, make the return value type of the method the type of the object to be created, and the method parameters are exactly the same as the constructor parameters to be mocked, the method name is arbitrary.

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

For complete code examples, see the `should_able_to_mock_new_object()` test case in the `java-demo` and `kotlin-demo` sample projects.

#### 5. Identify the current test case and invoke source

In the mock method, you can use `TestableTool.TEST_CASE` and `TestableTool.SOURCE_METHOD` to identify **the name of the currently running test case** and **the name of the method under test before entering the mock method**, so as to distinguish different invocation source.

> The implementation mechanism of these two fields is based on call stack analysis. Although various special cases have been dealt with, there is still the possibility of misjudgment in complex scenarios involving multiple threads. If you find a relevant reproducible BUG, please submit an issue on Github.

For complete code examples, see the `should_able_to_get_source_method_name()` and `should_able_to_get_test_case_name()` test cases in the `java-demo` and `kotlin-demo` sample projects.

#### 6. Verify the sequence and parameters of the mock method being invoked

In test cases, you can use the `TestableTool.verify()` method, and cooperate with `with()`, `withInOrder()`, `without()`, `withTimes()` and other methods to verify the mock call situation.

For details, please refer to the [Check Mock Call](en-us/doc/matcher.md) document.
