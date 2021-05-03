Frequently Asked Questions
---

#### 1. How to mock the fields initialized by `@Autowired` in the class under test?

Create the class under test object directly, and then use the ability of `TestableMock` to access private members to directly assign values to these fields.

#### 2. Can `TestableMock` be used with other mock tools?

`TestableMock` can be safely used together with other mock tools based on dynamic proxy mechanism, such as `Mockito`, `EasyMock`, `Spock`, etc., all belong to this category.

For mock tools that modify the class loader or the bytecode of the class under test, such as `PowerMock` and `JMockit`, there is no yet case to prove that they will conflict with `TestableMock`, but in principle, there may be a risk of incompatibility between the two, Please use with caution.

#### 3. Which test frameworks does `TestableMock` support?

The `PrivateAccessor` / `OmniConstructor` / `OmniAccessor` and basic functionality of `TestableMock` are theoretically test-frameworks-independent, and should work with any test frameworks out-of-box.

The only test-framework-related part is the mock invocation verifier, which currently supports `JUnit 4` / `JUnit 5` / `TestNG` and `Spock` appropriately.

If you encounter compatibility problem under a specific test framework, or want to add mock invocation verifier support for any other frameworks, please raise an [issue](https://github.com/alibaba/testable-mock/issues) to tell us.

#### 4. How to implement the mock method when the parent class variable points to the child class object?

In the code, there are often cases of using <u>interface variables or parent class variables</u> to point to an instance of subclasses and calling methods provided by the parent class or subclass.

At this time, follow a principle that the type of the first parameter of the mock method is always the same as the type of the variable that initiated the call.

Therefore, regardless of whether the actually called method comes from the parent class or the subclass, and whether the subclass overrides the method. If the calling variable is of the parent type (or interface type), the first parameter type of the mock method should use the corresponding parent type (or interface) type.

See the use case of the `DemoInheritTest` test class in the Java and Kotlin examples.

#### 5. How to mock generic methods (template methods)?

Just use the same generic parameters directly on the mock method.

See the use case of the `DemoTemplateTest` test class in the Java and Kotlin examples.

> Because JVM has a generic erasure mechanism, you can also directly use the `Object` type to replace generic parameters for Java projects, see the commented out "Second solution" example in the Java version of the `DemoTemplateTest` test class.

#### 6. How to mock invocation inside an inner class?

Just put mock methods in the mock container class of its outer class, it works for code in all inner classes.

See the use case of the `DemoInnerClass` test class in the Java and Kotlin examples.

#### 7. Why mocking methods in the `String` class in the Kotlin project does not work?

The `String` type in Kotlin language is actually `kotlin.String` instead of `java.lang.String`. However, when this type is built from bytecode, it will be replaced with Java's `java.lang.String` class, so no matter if the mock target is written as `kotlin.String` or `java.lang.String`, it cannot match the original called method.

In actual scenarios, there are very few scenarios where methods in the `String` class need to be mocked, so `TestableMock` has not dealt with this situation specifically.

#### 8. Can `TestableMock` be used for testing Android projects?

Yes, check `demo/android-demo` project for more information.

Please note, the `Dalvik` and `ART` virtual machines of the Android system use a bytecode system different from the standard JVM, which will affect the normal functionality of `TestableMock`.
If the test case require classes from Android SDK, it should be used in combination with [Roboelectric](https://github.com/robolectric/robolectric) testing framework.
This framework will run Android unit tests on a standard JVM virtual machine, which is much faster than running unit tests through the Android virtual machine, thus many Android App unit tests are written with the `Roboelectric` framework.

#### 9. Meet "Command Line is too Long. Shorten command line for ..." error when triggering test in IntelliJ IDE?

This problem is caused by the system `Class Path` content is too long, and has nothing to do with `TestableMock`. However, it should be noted that IntelliJ provides two auxiliary solutions: `JAR manifest` and `classpath file`. If `TestableMock` is used in the test, please select `JAR manifest`.

![jar-manifest](https://testable-code.oss-cn-beijing.aliyuncs.com/jar-manifest.png)
