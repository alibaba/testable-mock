TestableMock Introduction
---

The mock method in unit testing is usually to bypass method calls that rely on external resources or irrelevant functions, so that the focus of the test can be keep on the code logic that needs to be verified and guaranteed.

When defining the mock method, developers really care about only one thing: "<u>This call should be replaced with the fake **mock method** during testing</u>".

However, when the current mainstream mock framework implements the mock function, developers have to worry about too many things: how the mock framework is initialized, whether it is compatible with the unit testing framework used, whether the method to be mocked is private or static, whether the object to be mocked is created by `new` operator or injected, how to send the mock object back to the class under test... These non-critical additional tasks greatly distract the fun of using the mock tool.

Therefore, we developed `TestableMock`, **a maverick and lightweight mock tool**.


![mock-simpson-en-us.png](https://img.alicdn.com/imgextra/i2/O1CN01CdAfqR1tP2iqFC14g_!!6000000005893-2-tps-500-761.png)
