TestableMock Introduction
---

The Mock method in unit testing is usually to bypass method calls that rely on external resources or irrelevant functions, so that the focus of the test can be keep on the code logic that needs to be verified and guaranteed.

When defining the Mock method, the developer really cares about only one thing: "<u>This call should be replaced with the fake **mock method** during testing</u>".

However, when the current mainstream Mock framework implements the Mock function, developers have to worry about too many things: how the Mock framework is initialized, whether it is compatible with the unit testing framework used, whether the method to be mocked is private or static, whether the Mock object is created by `new` operator or injected, how to send the mock object back to the class under test... These non-critical additional tasks greatly distract the fun of using the mock tool.

Therefore, we developed `TestableMock`, **a maverick and lightweight mock tool**.


!#[mock](https://testable-code.oss-cn-beijing.aliyuncs.com/en-us/mock-simpson.png)
