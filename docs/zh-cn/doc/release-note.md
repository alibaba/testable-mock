# Release Note

## 0.4.2
- support change javaagent global log level via maven plugin
- fix an issue of duplicate test class injection
- fix an issue cause multiple method invocation mock fail

## 0.4.1
- deprecate @TestableMock annotation, use @MockMethod and @MockConstructor instead

## 0.4.0
- fix a jvm 9+ compatibility issue cause by default classloader change
- fix a conflict issue when testcase name duplicated between different class
- refactor code structure, module `testable-all` added
- separate constant definition from `TestableTool` to `TestableConst`

## 0.3.2
- support grable project for both private member access and quick mock
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
- support mock invoke by interface / base class object

## v0.2.0
- use `TestableTool` class to expose test context and verify mock invoke
- add `testable-maven-plugin` module to simplify javaagent configuration
- remove dependence on EnableTestable annotation in `testable-agent`
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
