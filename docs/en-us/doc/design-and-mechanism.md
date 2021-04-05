How Mock Works
---

This document mainly introduces the design and implementation principles of the mock function in `TestableMock`.

Unlike common mock-tools which write mock definitions in each test case, `TestableMock` allows each business class to provide its own set of mock methods, describing what should be mocked during testing, and define the corresponding replacement logic (that is, each business class has its own independent "Test class" and independent "Mock class"). By adopting "convention is better than configuration" principle, `TestableMock` not only reduce redundant codes but also reduce the cost of mock learning.

This design is based on two basic assumptions:

1. In any business class, methods that need to be mocked in one test case are usually required to be mocked in other test cases. For these mocked methods usually requiring external dependencies that are not easy to test.
2. Each unit test only focuses on the logic inside the unit under test, and irrelevant calls outside the unit should be replaced with mocks. That is, the invocations that need to be mocked should all be in the code of the class under test.

Accordingly, the unit test scenarios that meet the above assumptions are simplified through conventions, and the remaining more complex use scenarios are supported through configuration.

The mechanism of `TestableMock` can be summarized in one sentence: <u>Using java agent to dynamically modify the bytecode, before the unit test is about to run, replace all invocations in the business class under test which match the mock method definition with invocations to the mock method itself.</u>.

The final effect is that no matter what service framework or object-container the code uses, no matter whether the object of mock target is injected by a framework, created by new operation, and whether the target method of mock is private or external, defined as global, local, static, inherited or overloaded, all can be mocked in a same and simple way, which make unit testing much easier.

> Notice: Mock's goal is the method invocation in the class under test. The code inside the test case will not be mocked, and the method definition itself has not changed, but the invocation code to those methods will be replaced.

Specifically, when the unit test is started, `TestableMock` will preprocess the classes loaded into memory and establish the association relationship between the "class under test", the "test class", and the "mock container class" respectively (can be one-to-one or many-to-one). On the one hand, this association is to correctly match the mock call and replace it before the test case is executed, and on the other hand, it is used to control the effective scope of the mock method.

For the class under test, replace the matched call with a call to the mock container method.

For the test class, insert the mock context initialization code at the beginning of each test case.

For the mock container class, add the `testableIns()` method to make the class become a singleton class, and insert codes to record the call at the beginning of each Mock method.

The above is the core logic of the entire mocking logic. For more implementation details, please refer to the source code. If you have any questions, suggestions, or improvement proposals, you are welcome to participate in the discussion and contribute through Github Issue and Pull Request 😃
