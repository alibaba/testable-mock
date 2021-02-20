测试无返回值的方法
---

如何对void类型的方法进行测试一直是许多单元测试框架在悄悄回避的话题，由于以往的单元测试手段主要是对被测单元的返回结果进行校验，当遇到方法没有返回值时就会变得无从下手。

从功能的角度来说，虽然void方法不返回任何值，但它的执行一定会对外界产生某些潜在影响，我们将其称为方法的"副作用"，比如：

1. 初始化某些外部变量（私有成员变量或者全局静态变量）
2. 在方法体内对外部对象实例进行赋值   
3. 输出了日志
4. 调用了其他外部方法
5. ... ...


> 不返回任何值也不产生任何"副作用"的方法没有存在的意义。

这些"副作用"的本质归纳来说可分为两类：**修改外部变量**和**调用外部方法**。

通过`TestableMock`的私有字段访问和Mock校验器可以很方便的实现对"副作用"的结果检查。

### 1. 修改外部变量的void方法

例如，下面这个方法会根据输入修改私有成员变量`hashCache`：

```java
class Demo {
    private Map<String, Integer> hashCache = mapOf();

    public void updateCache(String domain, String key) {
        String cacheKey = domain + "::" + key;
        Integer num = hashCache.get(cacheKey);
        hashCache.put(cacheKey, count == null ? initHash(key) : nextHash(num, key));
    }

    ... // 其他方法省略
}
```

若要测试此方法，可以利用`TestableMock`直接读取私有成员变量的值，对结果进行校验：

```java
@EnablePrivateAccess  // 启用TestableMock的私有成员访问功能
class DemoTest {
    private Demo demo = new Demo();

    @Test
    public void testSaveToCache() {
        Integer firstVal = demo.initHash("hello"); // 访问私有方法
        Integer nextVal = demo.nextHash(firstVal, "hello"); // 访问私有方法
        demo.saveToCache("demo", "hello");
        assertEquals(firstVal, demo.hashCache.get("demo::hello")); // 读取私有变量
        demo.saveToCache("demo", "hello");
        assertEquals(nextVal, demo.hashCache.get("demo::hello")); // 读取私有变量
    }
}
```

### 2. 调用外部方法的void方法

例如，下面这个方法会根据输入打印信息到控制台：

```java
class Demo {
    public void recordAction(Action action) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ");
        String timeStamp = df.format(new Date());
        System.out.println(timeStamp + "[" + action.getType() + "] " + action.getTarget());
    }
}
```

若要测试此方法，可以利用`TestableMock`快速Mock掉`System.out.println`方法。在Mock方法体里可以继续执行原调用（相当于并不影响本来方法功能，仅用于做调用记录），也可以直接留空（相当于去除了原方法的副作用）。

在执行完被测的void类型方法以后，用`InvokeVerifier.verify()`校验传入的打印内容是否符合预期：

```java
class DemoTest {
    private Demo demo = new Demo();

    public static class Mock {
        // 拦截System.out.println调用
        @MockMethod
        public void println(PrintStream ps, String msg) {
            // 执行原调用
            ps.println(msg);
        }
    }

    @Test
    public void testRecordAction() {
        Action action = new Action("click", ":download");
        demo.recordAction();
        // 验证Mock方法println被调用，且传入参数格式符合预期
        verify("println").with(matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} \\[click\\] :download"));
    }
}
```
