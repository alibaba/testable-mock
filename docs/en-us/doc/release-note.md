# Release Note

## 0.6.6
- support global package path mapping for easier mocking code of 3rd packages
- complete mock target checking, improve resistance to code refactoring
- fix a log disorder issue of mock class handler
- fix an error of creating `Long` or `Integer` class with `OmniConstructor` in certain jvm version

## 0.6.5
- fix an issue cause `OmniConstructor` fail to handle classes like Date and BigDecimal
- fix an issue of `PrivateAccessor` fail to invoke method with single array parameter
- fix an issue of mock methods in super class are ignored when `scope` is `associated`
- fix an issue of empty mock method not works when `scope` is `associated`

## 0.6.4
- `TestableNull` class removed, let `OmniConstructor` even lighter
- support `@Nested` annotation of JUnit 5
- fix several `OmniConstructor` compatibility issues（thanks to [@ddatsh](https://github.com/ddatsh)）

## 0.6.3
- support custom inner `Mock` class name
- support skip specified packages from `OmniConstructor` enhancement
- fix 2 circle construction cases in `OmniConstructor`
- fix an index out-of-range issue caused by non-static method without `this` reference
- fix a null pointer exception caused by resource folder unavailable at runtime

## 0.6.2
- support setup global testable agent configure via properties file
- disable OmniConstructor bytecode enhancement by default
- fix a `Spring` framework compatibility issue with OmniConstructor

## 0.6.1
- generate mock scanning log file automatically, for self-troubleshooting
- fix a `Spock` test framework compatibility issue
- fix a `Gradle` unit test runtime compatibility issue

## 0.6.0
- add `OmniConstructor` and `OmniAccessor` tool for parameter preparation
- fix a `ClassFormatError` caused by incorrect `FRAME FULL` operation
- remove support for`diagnose` parameter of `@MockWith` annotation

## 0.5.2
- support use`PrivateAccessor` to access private member in parent class
- fix a class format error in jvm 1.8+ cause by incorrect bytecode generation
- fix illegal field modifiers error when add mock class to an interface

## 0.5.1
- use kotlin-style method descriptor in `VERBOSE` diagnose logs
- support parameterized test of JUnit 5
- fix an exception caused by method parameter with ternary operator
- fix a bug cause log message lost when `@MockWith` annotation used

## 0.4.12
- support verbose diagnose log for better self-troubleshooting
- support disable private access target existence check
- support specify mock scanning packages
- fix an ArrayIndexOutOfBoundsException issue when transforming native method

## 0.4.11
- support accessing private members of class under test in different package path 
- validate the number of private method parameters accessed by `PrivateAccessor`
- fix a bug which may cause errors when the mock method contains array parameters
- fix an issue which cause some private members not be found in the IntelliJ build

## 0.4.10
- fix an issue of using mock in lambda expression
- fix the NullPointerException when invoke private method with parameter value `null`

## 0.4.9
- fix an issue cause by improperly bytecode processing while using `targetClass` parameter
- auto validate access target of `PrivateAccessor`, improve resistance to code refactoring

## 0.4.8
- fix an issue of private method invocation failed in assignment statements
- support using mock for testing with `SpringRunner`
- support `targetClass` parameter in `@MockMethod` annotation

## 0.4.7
- fix incorrect stack size caused by `MOCK_CONTEXT` variable initialization
- fix incorrect gradle build path of under Windows operating system

## 0.4.6
- fix an issue of `IINC` bytecode processing
- support `TestableTool.MOCK_CONTEXT` variable to inject extra parameters to mock context
- use `TestableTool.TEST_CASE` variable to distinguish test case is no longer recommended

## 0.4.5
- fix private access compile error in intelliJ 2020.3+ environment
- change javaagent initialization logic to avoid NPE in cross-layer test scenario
- support dump transformed byte code to local file for issue investigation

## 0.4.4
- fix an issue of accessing private method with interface type parameter fail
- fix an issue of mocking static method without parameter fail 

## 0.4.3
- support static private member access

## 0.4.2
- support change javaagent global log level via maven plugin
- fix an issue of duplicate test class injection
- fix an issue cause multiple-line method invocation mock fail

## 0.4.1
- deprecate @TestableMock annotation, use @MockMethod and @MockConstructor instead

## 0.4.0
- fix a jvm 9+ compatibility issue cause by default classloader change
- fix a conflict issue when testcase name duplicated between different class
- refactor code structure, module `testable-all` added
- separate constant definition from `TestableTool` to `TestableConst`

## 0.3.2
- support gradle project for both private member access and quick mock
- support access private static field and methods via PrivateAccessor

## 0.3.1
- support detail log of mocking process for diagnosis

## v0.3.0
- add `without()` checker to verify mock method never invoked with specified parameters
- support fuzz matcher when verifying mock invocation

## v0.2.2
- support mock method parameters check
- fix a compatibility issue with jvm 9+

## v0.2.1
- support mock static method
- support mock kotlin companion object method
- support mock invoke by an interface / base class object

## v0.2.0
- use `TestableTool` class to expose test context and verify mock invoke
- add `testable-maven-plugin` module to simplify javaagent configuration
- remove dependence on `@EnableTestable` annotation in `testable-agent`
- rename annotations to reflect the actual use

## v0.1.0
- move generated agent jar to class folder
- support mock method of any object

## v0.0.5
- use dynamically runtime modification to replace static `e.java` file
- get rid of unit test framework dependence
- add testable ref field in test class at runtime instead of compile time

## v0.0.4
- use runtime byte code rewrite to invoke testable setup method
- add `TestableUtil` class to fetch current test case and invocation source

## v0.0.3
- use global method invoke to access private members instead of modification in place
- use `e.java` replace `testable` class make code more readable
- introduce `agent` module, use runtime byte code modification to support new operation and member method mocking

## v0.0.2
- add support of member method mocking by compile time code modification

## v0.0.1
- PoC version
- use compile time code modification to support new operation mocking and private field & method access
