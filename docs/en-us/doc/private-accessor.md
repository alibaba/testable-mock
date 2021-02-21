Private Accessor
---

Nowadays, the debate about whether private methods should be unit tested is gradually disappearing, and the common practice of developers has given factual answers. Indirect testing of private methods through public methods is difficult in many cases. Developers are more willing to modify method visibility to make original private methods testable in test cases.

In addition, before unit testing begin, it is often necessary to initialize specific member fields of the object under test, but sometimes it could be impossible to easily assign values to these private fields. So, is it possible let the code in the unit test case directly access the private methods and member fields of the class under test without breaking the encapsulation of the type under test? `TestableMock` provides two simple solutions.

### Solution 1: Use `@EnablePrivateAccess` annotation

Just add `@EnablePrivateAccess` annotation to the test class, then you have got the following enhancements in the test case:

- Invoke private methods (including static methods) of the **class under test**
- Read private fields (including static fields) of the **class under test**
- Modify private fields (including static fields) of the **class under test**
- Modify the constant fields of the **class under test** (fields modified with final, including static fields)

When accessing and modifying private and constant members, the IDE may prompt some syntax errors, but the compiler will be able to run the test normally.

For the effect, see the use case in the test class of the `java-demo` sample project `DemoPrivateAccessTest`. (Using compile-time code enhancement, currently only the adaptation of the Java language is implemented)

> This function assumes that the test class is in the same package as the class under test, and the name is `<ClassUnderTest>+Test`. When this convention is not met, you can use the `srcClass` parameter on the `@EnablePrivateAccess` annotation to specify the actual class under test. E.g:
>
> ```java
> @EnablePrivateAccess(srcClass = DemoServiceImpl.class)
> class DemoServiceTest() { ... }
> ```

### Solution 2: Use the `PrivateAccessor` tool class

If you don't want to see the IDE's syntax error reminder, or in a non-Java language JVM project (such as Kotlin language), you can also use the `PrivateAccessor` tool class to directly access private members.

This class provides 7 static methods:

- `PrivateAccessor.get(<AnyObject>, "<private-field-name>")` ➜ read the private field of any object
- `PrivateAccessor.set(<AnyObject>, "<private-field-name>", <new-value>)` ➜ modify the private field (or constant field) of any object
- `PrivateAccessor.invoke(<AnyObject>, "<private-method-name>", <call-parameters>...)` ➜ call the private method of any object
- `PrivateAccessor.getStatic(<AnyClass>, "<private-static-field-name>")` ➜ read the **static** private field of any class
- `PrivateAccessor.setStatic(<AnyClass>, "<private-static-field-name>", <new-value>)` ➜ modify the **static** private field (or **static** constant field) of any class
- `PrivateAccessor.invokeStatic(<AnyClass>, "<private-static-method-name>", <call-parameters>...)` ➜ call the **static** private method of any class
- `PrivateAccessor.construct(<AnyClass>, <constructor-parameters>...)` ➜ create a new object by the private constructor of any class

> Using the `PrivateAccessor` class does not require the test class to have `@EnablePrivateAccess` annotation, but adding this annotation will enable the compile-time verification for the private members of the class under test.

For details, see the use cases in the test classes of the `java-demo` and `kotlin-demo` sample projects `DemoPrivateAccessTest`.

### Compile-time verification of private members

Both of the above two methods essentially use JVM reflection mechanism to achieve private member access, but the JVM compiler will not check the existence of the reflection target. When the code is refactored, if the private method names and parameters in the source class are modified, it would cause exceptions to be discovered only when the unit test is triggered. For this reason, another function of the `@EnablePrivateAccess` annotation is to perform additional compile-time checks for existence of private member of the **class under test**.

**Note**: When the private member verification function is enabled, the `PrivateAccessor` class can only be used to access the private members of the **class under test**, which will help limit the using of the `PrivateAccessor` tool class for "unauthorized" operations unrelated to the current test. If you really need to access private members of other classes, you can remove the `@EnablePrivateAccess` annotation, or set the `verifyTargetOnCompile` parameter of the annotation to `false` to manually turn off the verification function.
