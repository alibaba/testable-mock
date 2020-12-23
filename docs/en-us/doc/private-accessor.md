Private Accessor
---

Nowadays, the debate about whether private methods should be unit tested is gradually disappearing, and the common practice of developers has given factual answers. Indirect testing of private methods through public methods is difficult in many cases. Developers are more willing to modify method visibility to make original private methods testable in test cases.

In addition, before unit testing begin, it is often necessary to initialize specific member fields of the object under test, but sometimes it could be impossible to easily assign values to these private fields. So, is it possible let the code in the unit test case directly access the private methods and member fields of the class under test without breaking the encapsulation of the tested type? `TestableMock` provides two simple solutions.

### Solution 1: Use `@EnablePrivateAccess` annotation

Just add `@EnablePrivateAccess` annotation to the test class, then you have got the following enhancements in the test case:

- Invoke private methods (including static methods) of the class under test
- Read private fields (including static fields) of the class under test
- Modify private fields (including static fields) of the class under test
- Modify the constant fields of the class under test (fields modified with final, including static fields)

When accessing and modifying private and constant members, the IDE may prompt some syntax errors, but the compiler will be able to run the test normally.

For the effect, see the use case in the test class of the `java-demo` sample project `DemoPrivateAccessTest`. (Using compile-time code enhancement, currently only the adaptation of the Java language is implemented)

### Solution 2: Use the `PrivateAccessor` tool class

If you don't want to see the IDE's syntax error reminder, or in a non-Java language JVM project (such as Kotlin language), you can also use the `PrivateAccessor` tool class to directly access private members.

This class provides 6 static methods:

- `PrivateAccessor.get(<ObjectUnderTest>, "<private-field-name>")` ➜ read the private field of the class under test
- `PrivateAccessor.set(<ObjectUnderTest>, "<private-field-name>", <new-value>)` ➜ modify the private field (or constant field) of the class under test
- `PrivateAccessor.invoke(<ObjectUnderTest>, "<private-method-name>", <call-parameters>..)` ➜ call the private method of the class under test
- `PrivateAccessor.getStatic(<ClassUnderTest>, "<private-static-field-name>")` ➜ read the **static** private field of the class under test
- `PrivateAccessor.setStatic(<ClassUnderTest>, "<private-static-field-name>", <new-value>)` ➜ modify the **static** private field (or **static** constant field) of the class under test
- `PrivateAccessor.invokeStatic(<ClassUnderTest>, "<private-static-method-name>", <call-parameters>..)` ➜ call the **static** private method of the class under test

For details, see the use cases in the test classes of the `java-demo` and `kotlin-demo` sample projects `DemoPrivateAccessTest`.
