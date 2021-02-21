Test Void Method
---

"How to test void type methods" has always been a topic that many unit testing frameworks are quietly avoiding. Since the existing unit testing methods are mainly to verify the returned results of the tested unit, when the method has no return value, there is no way to test.

From a functional point of view, although the void method does not return any value, its execution will definitely have some potential impact on the outside world. We call it the "side effect" of the method, such as:

1. Initialize external variables (private member variables or global static variables)
2. Assign value to external object
3. Print logs
4. Invoke other external methods
5. ... ...

> A method that does not return any value and does not produce any "side effects" has no meaning to exist.

The essence of these "side effects" can be summarized into two categories: **modify external variables** and **invoke external methods**.

Through the private field accessor and the mock validator of `TestableMock`, the "side effects" can be easily checked.

### 1. Void type method which modify external variables

For example, the following method will modify the private member variable `hashCache` based on the input:

```java
class Demo {
    private Map<String, Integer> hashCache = mapOf();

    public void updateCache(String domain, String key) {
        String cacheKey = domain + "::" + key;
        Integer num = hashCache.get(cacheKey);
        hashCache.put(cacheKey, count == null ? initHash(key) : nextHash(num, key));
    }

    ... // Other methods omitted
}
```

To test this method, you can use `TestableMock` directly read the value of the private member variable and verify the result:

```java
@EnablePrivateAccess  // Enable private member access functionality of TestableMock
class DemoTest {
    private Demo demo = new Demo();

    @Test
    public void testSaveToCache() {
        Integer firstVal = demo.initHash("hello"); // Invoke private method
        Integer nextVal = demo.nextHash(firstVal, "hello"); // Invoke private method
        demo.saveToCache("demo", "hello");
        assertEquals(firstVal, demo.hashCache.get("demo::hello")); // Access private variable
        demo.saveToCache("demo", "hello");
        assertEquals(nextVal, demo.hashCache.get("demo::hello")); // Access private variable
    }
}
```

### 2. Void type method which invoke external method

For example, the following method will print information to the console based on input:

```java
class Demo {
    public void recordAction(Action action) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ");
        String timeStamp = df.format(new Date());
        System.out.println(timeStamp + "[" + action.getType() + "] " + action.getTarget());
    }
}
```

To test this method, you can use `TestableMock` to quickly mock out the `System.out.println` method. In the mock method body, you can simply call the original method (equivalent to not affecting the original method function, only used for call recording), or leave it blank (equivalent to removing the side effects of the original method).

After executing the void type method under test, use `InvokeVerifier.verify()` to verify whether the incoming print content meets expectations:

```java
class DemoTest {
    private Demo demo = new Demo();

    public static class Mock {
        // Intercept `System.out.println` invocation
        @MockMethod
        public void println(PrintStream ps, String msg) {
            // Execute the original call
            ps.println(msg);
        }
    }

    @Test
    public void testRecordAction() {
        Action action = new Action("click", ":download");
        demo.recordAction();
        // Verify mock method `println` is invoked, and passing parameters in line with expectations 
        verify("println").with(matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} \\[click\\] :download"));
    }
}
```
